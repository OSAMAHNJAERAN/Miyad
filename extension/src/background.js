import { emailFingerprintSource, sha256 } from "./utils/hasher.js";

export const STORAGE = {
  TOKEN: "miyad_user_token",
  USER: "miyad_user",
  API_URL: "miyad_api_url",
  HASHES: "miyad_processed_hashes",
  RECENT: "miyad_recent_events",
  QUEUE: "miyad_retry_queue"
};
const DEFAULT_API_URL = "http://127.0.0.1:8000";
const MAX_HASHES = 500;
const MAX_RECENT = 8;
const RETRY_ALARM = "miyad-retry";
const REQUEST_TIMEOUT_MS = 15_000;

async function syncGet(keys) {
  return chrome.storage.sync.get(keys);
}

async function localGet(keys) {
  return chrome.storage.local.get(keys);
}

async function apiConfig() {
  const values = await syncGet([STORAGE.TOKEN, STORAGE.API_URL]);
  return {
    token: values[STORAGE.TOKEN] || "",
    apiUrl: (values[STORAGE.API_URL] || DEFAULT_API_URL).replace(/\/$/, "")
  };
}

export async function rememberHash(hash) {
  const values = await localGet(STORAGE.HASHES);
  const hashes = values[STORAGE.HASHES] || [];
  const next = [hash, ...hashes.filter((item) => item !== hash)].slice(0, MAX_HASHES);
  await chrome.storage.local.set({ [STORAGE.HASHES]: next });
}

export async function wasProcessed(hash) {
  const values = await localGet(STORAGE.HASHES);
  return (values[STORAGE.HASHES] || []).includes(hash);
}

async function rememberRecent(events) {
  if (!events?.length) return;
  const values = await localGet(STORAGE.RECENT);
  const recent = values[STORAGE.RECENT] || [];
  const next = [
    ...events.map((event) => ({
      title: event.title,
      event_type: event.event_type,
      due_date: event.due_date,
      captured_at: new Date().toISOString()
    })),
    ...recent
  ].slice(0, MAX_RECENT);
  await chrome.storage.local.set({ [STORAGE.RECENT]: next });
}

export async function queueRequest(item) {
  const values = await localGet(STORAGE.QUEUE);
  const queue = values[STORAGE.QUEUE] || [];
  const existing = queue.find((entry) => entry.emailHash === item.emailHash);
  if (!existing) {
    queue.push({ ...item, attempts: 0, nextAttemptAt: Date.now() + 60_000 });
    await chrome.storage.local.set({ [STORAGE.QUEUE]: queue });
  }
  await chrome.alarms.create(RETRY_ALARM, { delayInMinutes: 1 });
}

export class ApiError extends Error {
  constructor(message, status = 0) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.retryable = status === 0 || status === 429 || status >= 500;
  }
}

export function normalizeApiUrl(value) {
  const url = new URL(value);
  if (!["http:", "https:"].includes(url.protocol)) {
    throw new Error("Backend URL must use HTTP or HTTPS.");
  }
  if (url.username || url.password) {
    throw new Error("Backend URL must not contain credentials.");
  }
  return url.origin;
}

export async function callApi(path, options = {}) {
  const { token, apiUrl } = await apiConfig();
  const headers = { "Content-Type": "application/json", ...(options.headers || {}) };
  if (token) headers.Authorization = `Bearer ${token}`;
  let response;
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);
  try {
    response = await fetch(`${apiUrl}${path}`, {
      ...options,
      headers,
      signal: options.signal || controller.signal
    });
  } catch (error) {
    throw new ApiError(error.message || "Network request failed");
  } finally {
    clearTimeout(timeout);
  }
  const body = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new ApiError(
      typeof body.detail === "string"
        ? body.detail
        : `Request failed (${response.status})`,
      response.status
    );
  }
  return body;
}

export async function processEmail(email, force = false) {
  const { token } = await apiConfig();
  if (!token) throw new Error("Connect your Miyad account first.");

  const emailHash = await sha256(emailFingerprintSource(email));
  if (!force && (await wasProcessed(emailHash))) {
    return { status: "already_processed", events_created: 0 };
  }
  const payload = {
    metadata: {
      subject: email.subject || "",
      sender: email.sender || "",
      timestamp: email.timestamp || new Date().toISOString(),
      timezone: email.timezone || Intl.DateTimeFormat().resolvedOptions().timeZone
    },
    raw_content: email.body,
    email_hash: emailHash
  };

  try {
    const result = await callApi("/api/process-email", {
      method: "POST",
      body: JSON.stringify(payload)
    });
    if (["success", "already_processed", "no_events_found"].includes(result.status)) {
      await rememberHash(emailHash);
    }
    await rememberRecent(result.events);
    await sendHeartbeat().catch(() => {});
    return result;
  } catch (error) {
    if (error.retryable !== false) {
      await queueRequest({ emailHash, payload });
    }
    throw error;
  }
}

export async function processRetryQueue() {
  const values = await localGet(STORAGE.QUEUE);
  const queue = values[STORAGE.QUEUE] || [];
  const remaining = [];

  for (const item of queue) {
    if (item.nextAttemptAt > Date.now()) {
      remaining.push(item);
      continue;
    }
    try {
      const result = await callApi(item.path || "/api/process-email", {
        method: "POST",
        body: JSON.stringify(item.payload)
      });
      if (item.rememberHash !== false) {
        await rememberHash(item.emailHash);
      }
      await rememberRecent(result.events);
      await sendHeartbeat().catch(() => {});
    } catch (error) {
      if (error.retryable === false) continue;
      const attempts = item.attempts + 1;
      if (attempts < 5) {
        remaining.push({
          ...item,
          attempts,
          nextAttemptAt: Date.now() + Math.min(2 ** attempts * 60_000, 60 * 60_000)
        });
      }
    }
  }
  await chrome.storage.local.set({ [STORAGE.QUEUE]: remaining });
  if (remaining.length) {
    await chrome.alarms.create(RETRY_ALARM, { delayInMinutes: 1 });
  }
}

export async function sendHeartbeat() {
  return callApi("/api/extension/heartbeat", { method: "POST" });
}

chrome.runtime.onInstalled.addListener(() => {
  chrome.contextMenus.removeAll(() => {
    chrome.contextMenus.create({
      id: "send-to-miyad",
      title: "Send to Miyad / إرسال إلى ميعاد",
      contexts: ["selection"],
      documentUrlPatterns: [
        "https://outlook.office.com/*",
        "https://outlook.live.com/*"
      ]
    });
  });
});

chrome.contextMenus.onClicked.addListener(async (info) => {
  if (info.menuItemId !== "send-to-miyad" || !info.selectionText) return;
  const payload = {
    raw_content: info.selectionText,
    subject: "Outlook selection",
    sender: "manual-context-menu",
    timestamp: new Date().toISOString(),
    timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
    save: true
  };
  try {
    const result = await callApi("/api/extract-manual", {
      method: "POST",
      body: JSON.stringify(payload)
    });
    await rememberRecent(result.events);
    await sendHeartbeat().catch(() => {});
  } catch (error) {
    if (error.retryable !== false) {
      await queueRequest({
        emailHash: await sha256(`manual:${info.selectionText}`),
        path: "/api/extract-manual",
        payload,
        rememberHash: false
      });
    }
    console.error("[Miyad] Context menu extraction failed", error);
  }
});

chrome.alarms.onAlarm.addListener((alarm) => {
  if (alarm.name === RETRY_ALARM) processRetryQueue();
});

chrome.runtime.onMessage.addListener((message, _sender, sendResponse) => {
  const respond = async () => {
    switch (message.type) {
      case "PROCESS_EMAIL":
        return processEmail(message.email);
      case "LOGIN": {
        const result = await callApi("/api/auth/login", {
          method: "POST",
          body: JSON.stringify(message.payload)
        });
        await chrome.storage.sync.set({
          [STORAGE.TOKEN]: result.token,
          [STORAGE.USER]: result.user
        });
        await sendHeartbeat().catch(() => {});
        return result;
      }
      case "REGISTER": {
        const result = await callApi("/api/auth/register", {
          method: "POST",
          body: JSON.stringify(message.payload)
        });
        await chrome.storage.sync.set({
          [STORAGE.TOKEN]: result.token,
          [STORAGE.USER]: result.user
        });
        await sendHeartbeat().catch(() => {});
        return result;
      }
      case "LOGOUT":
        await chrome.storage.sync.remove([STORAGE.TOKEN, STORAGE.USER]);
        return { success: true };
      case "STATUS": {
        const sync = await syncGet([STORAGE.TOKEN, STORAGE.USER, STORAGE.API_URL]);
        if (sync[STORAGE.TOKEN]) {
          await sendHeartbeat().catch(() => {});
        }
        const local = await localGet([STORAGE.RECENT, STORAGE.QUEUE]);
        return {
          connected: Boolean(sync[STORAGE.TOKEN]),
          user: sync[STORAGE.USER] || null,
          apiUrl: sync[STORAGE.API_URL] || DEFAULT_API_URL,
          recent: local[STORAGE.RECENT] || [],
          queued: (local[STORAGE.QUEUE] || []).length
        };
      }
      case "SET_API_URL":
        await chrome.storage.sync.set({
          [STORAGE.API_URL]: normalizeApiUrl(message.apiUrl)
        });
        return { success: true };
      default:
        throw new Error("Unknown message");
    }
  };
  respond()
    .then((data) => sendResponse({ ok: true, data }))
    .catch((error) => sendResponse({ ok: false, error: error.message }));
  return true;
});
