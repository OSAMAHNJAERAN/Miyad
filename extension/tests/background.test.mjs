import assert from "node:assert/strict";
import test from "node:test";

const syncData = {};
const localData = {};
const listeners = {
  installed: null,
  contextMenu: null,
  alarm: null,
  message: null
};

function storageArea(data) {
  return {
    async get(keys) {
      const requested = Array.isArray(keys) ? keys : [keys];
      return Object.fromEntries(
        requested
          .filter((key) => Object.hasOwn(data, key))
          .map((key) => [key, data[key]])
      );
    },
    async set(values) {
      Object.assign(data, values);
    },
    async remove(keys) {
      for (const key of Array.isArray(keys) ? keys : [keys]) delete data[key];
    }
  };
}

globalThis.chrome = {
  storage: {
    sync: storageArea(syncData),
    local: storageArea(localData)
  },
  alarms: {
    created: [],
    async create(name, options) {
      this.created.push({ name, options });
    },
    onAlarm: {
      addListener(listener) {
        listeners.alarm = listener;
      }
    }
  },
  runtime: {
    onInstalled: {
      addListener(listener) {
        listeners.installed = listener;
      }
    },
    onMessage: {
      addListener(listener) {
        listeners.message = listener;
      }
    }
  },
  contextMenus: {
    removeAll(callback) {
      callback();
    },
    create() {},
    onClicked: {
      addListener(listener) {
        listeners.contextMenu = listener;
      }
    }
  }
};

const background = await import("../src/background.js");
const { STORAGE } = background;

function reset() {
  for (const key of Object.keys(syncData)) delete syncData[key];
  for (const key of Object.keys(localData)) delete localData[key];
  chrome.alarms.created.length = 0;
  syncData[STORAGE.TOKEN] = "token";
  syncData[STORAGE.API_URL] = "http://127.0.0.1:8000";
}

function okJson(body) {
  return {
    ok: true,
    status: 200,
    async json() {
      return body;
    }
  };
}

function sendMessage(message) {
  return new Promise((resolve) => {
    listeners.message(message, null, resolve);
  });
}

const email = {
  subject: "Database Assignment",
  sender: "lecturer@university.edu",
  timestamp: "2026-06-11T10:00:00+08:00",
  timezone: "Asia/Kuala_Lumpur",
  body: "Submit by Friday at 11:59 PM."
};

test("processEmail stores a local hash and skips duplicates", async () => {
  reset();
  const urls = [];
  globalThis.fetch = async (url) => {
    urls.push(url);
    if (url.endsWith("/api/extension/heartbeat")) {
      return okJson({ connected: true });
    }
    return okJson({
      status: "success",
      events_created: 1,
      events: [{
        title: "Database Assignment",
        event_type: "deadline",
        due_date: "2026-06-12T23:59:00+08:00"
      }]
    });
  };

  assert.equal((await background.processEmail(email)).status, "success");
  assert.equal((await background.processEmail(email)).status, "already_processed");
  assert.equal(urls.filter((url) => url.endsWith("/api/process-email")).length, 1);
  assert.equal(urls.filter((url) => url.endsWith("/api/extension/heartbeat")).length, 1);
  assert.equal(localData[STORAGE.HASHES].length, 1);
  assert.equal(localData[STORAGE.RECENT].length, 1);
});

test("network failures queue once and a later retry clears the queue", async () => {
  reset();
  globalThis.fetch = async () => {
    throw new Error("offline");
  };

  await assert.rejects(background.processEmail(email), /Could not reach/);
  await assert.rejects(background.processEmail(email), /Could not reach/);
  assert.equal(localData[STORAGE.QUEUE].length, 1);
  localData[STORAGE.QUEUE][0].nextAttemptAt = 0;

  globalThis.fetch = async () => okJson({
    status: "success",
    events_created: 0,
    events: []
  });
  await background.processRetryQueue();

  assert.deepEqual(localData[STORAGE.QUEUE], []);
  assert.equal(localData[STORAGE.HASHES].length, 1);
});

test("authentication failures are not added to the retry queue", async () => {
  reset();
  globalThis.fetch = async () => ({
    ok: false,
    status: 401,
    async json() {
      return { detail: "Invalid token" };
    }
  });

  await assert.rejects(background.processEmail(email), /Invalid token/);
  assert.equal(localData[STORAGE.QUEUE], undefined);
});

test("context menu selection uses the manual extraction endpoint", async () => {
  reset();
  let request = null;
  globalThis.fetch = async (url, options) => {
    if (url.endsWith("/api/extract-manual")) request = { url, options };
    return okJson({ status: "success", events_created: 0, events: [] });
  };

  await listeners.contextMenu({
    menuItemId: "send-to-miyad",
    selectionText: "Quiz next Tuesday at 10 AM."
  });

  assert.ok(request.url.endsWith("/api/extract-manual"));
  const body = JSON.parse(request.options.body);
  assert.equal(body.raw_content, "Quiz next Tuesday at 10 AM.");
  assert.equal(body.save, true);
  assert.ok(body.timezone);
});

test("offline context-menu extraction is retried through its original endpoint", async () => {
  reset();
  globalThis.fetch = async () => {
    throw new Error("offline");
  };
  const originalConsoleError = console.error;
  console.error = () => {};
  try {
    await listeners.contextMenu({
      menuItemId: "send-to-miyad",
      selectionText: "Quiz next Tuesday at 10 AM."
    });
  } finally {
    console.error = originalConsoleError;
  }
  assert.equal(localData[STORAGE.QUEUE].length, 1);
  assert.equal(localData[STORAGE.QUEUE][0].path, "/api/extract-manual");
  assert.equal(localData[STORAGE.QUEUE][0].rememberHash, false);
  localData[STORAGE.QUEUE][0].nextAttemptAt = 0;

  let retryUrl = "";
  globalThis.fetch = async (url) => {
    if (url.endsWith("/api/extract-manual")) retryUrl = url;
    return okJson({ status: "success", events_created: 0, events: [] });
  };
  await background.processRetryQueue();

  assert.ok(retryUrl.endsWith("/api/extract-manual"));
  assert.deepEqual(localData[STORAGE.QUEUE], []);
  assert.equal(localData[STORAGE.HASHES], undefined);
});

test("normalizes safe backend URLs and rejects embedded credentials", () => {
  assert.equal(
    background.normalizeApiUrl("https://api.example.edu/path/"),
    "https://api.example.edu"
  );
  assert.throws(
    () => background.normalizeApiUrl("https://user:pass@example.edu"),
    /credentials/
  );
});

test("login stores the token and sends an authenticated extension heartbeat", async () => {
  reset();
  delete syncData[STORAGE.TOKEN];
  const requests = [];
  globalThis.fetch = async (url, options) => {
    requests.push({ url, options });
    if (url.endsWith("/api/auth/login")) {
      return okJson({
        token: "new-token",
        user: { email: "student@example.edu" }
      });
    }
    return okJson({ connected: true });
  };

  const response = await sendMessage({
    type: "LOGIN",
    payload: { email: "student@example.edu", password: "password" }
  });

  assert.equal(response.ok, true);
  assert.equal(syncData[STORAGE.TOKEN], "new-token");
  assert.equal(syncData[STORAGE.USER].email, "student@example.edu");
  assert.ok(requests[1].url.endsWith("/api/extension/heartbeat"));
  assert.equal(requests[1].options.headers.Authorization, "Bearer new-token");
});

test("local hash cache remains bounded to 500 newest entries", async () => {
  reset();
  for (let index = 0; index < 510; index += 1) {
    await background.rememberHash(`sha256:${index.toString().padStart(64, "0")}`);
  }

  assert.equal(localData[STORAGE.HASHES].length, 500);
  assert.ok(localData[STORAGE.HASHES][0].endsWith("509"));
  assert.ok(localData[STORAGE.HASHES][499].endsWith("010"));
});

test("status reports backend health and configured URL", async () => {
  reset();
  globalThis.fetch = async (url) => {
    if (url.endsWith("/health")) {
      return okJson({
        status: "ok",
        environment: "test",
        database: "memory"
      });
    }
    return okJson({ connected: true });
  };

  const response = await sendMessage({ type: "STATUS" });
  assert.equal(response.ok, true);
  assert.equal(response.data.backend.reachable, true);
  assert.equal(response.data.backend.apiUrl, "http://127.0.0.1:8000");
  assert.equal(response.data.backend.database, "memory");
});

test("health check returns actionable details when backend is offline", async () => {
  reset();
  globalThis.fetch = async () => {
    throw new Error("offline");
  };

  const health = await background.checkBackend();
  assert.equal(health.reachable, false);
  assert.match(health.error, /127\.0\.0\.1:8000/);
  assert.match(health.error, /running/);
});

test("theme and language preferences normalize and persist", async () => {
  reset();
  delete syncData[STORAGE.TOKEN];
  globalThis.fetch = async () => okJson({});

  const themeResponse = await sendMessage({
    type: "SET_THEME",
    theme: "dark"
  });
  const languageResponse = await sendMessage({
    type: "SET_LANGUAGE",
    language: "ar-SA"
  });

  assert.equal(themeResponse.data.theme, "dark");
  assert.equal(languageResponse.data.language, "ar");
  assert.equal(syncData[STORAGE.THEME], "dark");
  assert.equal(syncData[STORAGE.LANGUAGE], "ar");
  assert.equal(background.normalizeTheme("unknown"), "system");
  assert.equal(background.normalizeLanguage("fr-FR"), "en");
});

test("authenticated language preferences synchronize through api settings", async () => {
  reset();
  syncData[STORAGE.USER] = {
    email: "student@example.edu",
    preferred_language: "en"
  };
  let patch = null;
  globalThis.fetch = async (url, options = {}) => {
    if (url.endsWith("/api/settings") && options.method === "PATCH") {
      patch = JSON.parse(options.body);
      return okJson({ preferred_language: patch.preferred_language });
    }
    return okJson({ preferred_language: "ar" });
  };

  const response = await sendMessage({
    type: "SET_LANGUAGE",
    language: "ar"
  });

  assert.equal(response.ok, true);
  assert.deepEqual(patch, { preferred_language: "ar" });
  assert.equal(syncData[STORAGE.USER].preferred_language, "ar");
});

test("authenticated UI preferences use the account language", async () => {
  reset();
  syncData[STORAGE.THEME] = "light";
  syncData[STORAGE.LANGUAGE] = "en";
  syncData[STORAGE.USER] = { email: "student@example.edu" };
  globalThis.fetch = async () => okJson({ preferred_language: "ar" });

  const response = await sendMessage({ type: "UI_PREFERENCES" });

  assert.equal(response.data.theme, "light");
  assert.equal(response.data.language, "ar");
  assert.equal(response.data.hasStoredLanguage, true);
  assert.equal(syncData[STORAGE.LANGUAGE], "ar");
});
