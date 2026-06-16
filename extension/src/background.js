import { emailFingerprintSource, sha256 } from "./utils/hasher.js";

export const STORAGE = {
  TOKEN: "miyad_user_token",
  USER: "miyad_user",
  API_URL: "miyad_api_url",
  THEME: "miyad_theme",
  LANGUAGE: "miyad_prelogin_language",
  HASHES: "miyad_processed_hashes",
  RECENT: "miyad_recent_events",
  QUEUE: "miyad_retry_queue"
};
const DEFAULT_API_URL = "http://127.0.0.1:8000";
const MAX_HASHES = 500;
const MAX_RECENT = 8;
const RETRY_ALARM = "miyad-retry";
const REQUEST_TIMEOUT_MS = 15_000;
const THEMES = new Set(["system", "light", "dark"]);
const LANGUAGES = new Set(["ar", "en"]);
let currentEmail = null;
let pendingPreview = null;
const SESSION_PREVIEW = "miyad_pending_preview";

async function syncGet(keys) {
  return chrome.storage.sync.get(keys);
}

async function localGet(keys) {
  return chrome.storage.local.get(keys);
}

async function setPendingPreview(value) {
  pendingPreview = value;
  if (!chrome.storage.session) return;
  if (value) {
    await chrome.storage.session.set({ [SESSION_PREVIEW]: value });
  } else {
    await chrome.storage.session.remove(SESSION_PREVIEW);
  }
}

async function getPendingPreview() {
  if (pendingPreview) return pendingPreview;
  if (!chrome.storage.session) return null;
  const values = await chrome.storage.session.get(SESSION_PREVIEW);
  pendingPreview = values[SESSION_PREVIEW] || null;
  return pendingPreview;
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

export function normalizeTheme(value) {
  return THEMES.has(value) ? value : "system";
}

export function normalizeLanguage(value) {
  const language = String(value || "").toLowerCase().split("-")[0];
  return LANGUAGES.has(language) ? language : "en";
}

async function accountLanguage() {
  const values = await syncGet([
    STORAGE.TOKEN,
    STORAGE.USER,
    STORAGE.LANGUAGE
  ]);
  let language = normalizeLanguage(
    values[STORAGE.LANGUAGE] || values[STORAGE.USER]?.preferred_language
  );
  if (values[STORAGE.TOKEN]) {
    try {
      const settings = await callApi("/api/settings");
      language = normalizeLanguage(settings.preferred_language || language);
      await chrome.storage.sync.set({
        [STORAGE.LANGUAGE]: language,
        [STORAGE.USER]: {
          ...(values[STORAGE.USER] || {}),
          preferred_language: language
        }
      });
    } catch {
      // The stored language keeps the popup usable while the backend is offline.
    }
  }
  return language;
}

export async function getUiPreferences() {
  const values = await syncGet([
    STORAGE.THEME,
    STORAGE.LANGUAGE,
    STORAGE.TOKEN
  ]);
  return {
    theme: normalizeTheme(values[STORAGE.THEME]),
    language: await accountLanguage(),
    hasStoredLanguage: Boolean(
      values[STORAGE.LANGUAGE] || values[STORAGE.TOKEN]
    )
  };
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
    const reason = error?.name === "AbortError"
      ? "The backend request timed out."
      : "Could not reach the Miyad backend.";
    throw new ApiError(
      `${reason} Check that ${apiUrl} is running and allowed in the extension settings.`
    );
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

export async function checkBackend() {
  const { apiUrl } = await apiConfig();
  try {
    const health = await callApi("/health");
    return {
      reachable: health.status === "ok",
      apiUrl,
      environment: health.environment || "",
      database: health.database || "",
      aiConfigured: health.ai_configured !== false
    };
  } catch (error) {
    return {
      reachable: false,
      apiUrl,
      error: error.message
    };
  }
}

function buildEmailPayload(email) {
  return {
    metadata: {
      subject: email.subject || "",
      sender: email.sender || "",
      timestamp: email.timestamp || new Date().toISOString(),
      timezone: email.timezone || Intl.DateTimeFormat().resolvedOptions().timeZone
    },
    raw_content: email.body,
    email_hash: ""
  };
}

async function currentOutlookEmail() {
  if (chrome.tabs?.query && chrome.tabs?.sendMessage) {
    try {
      const [tab] = await chrome.tabs.query({ active: true, lastFocusedWindow: true });
      if (tab?.id) {
        const response = await chrome.tabs.sendMessage(tab.id, {
          type: "GET_CURRENT_EMAIL"
        });
        if (response?.email) currentEmail = response.email;
      }
    } catch {
      // The popup may be opened outside Outlook; the last in-memory detection is safe.
    }
  }
  return currentEmail;
}

export async function previewCurrentEmail() {
  const { token } = await apiConfig();
  if (!token) throw new Error("Connect your Miyad account first.");
  const email = await currentOutlookEmail();
  if (!email?.body) throw new Error("Open an Outlook email first, then try again.");
  const payload = buildEmailPayload(email);
  payload.email_hash = await sha256(emailFingerprintSource(email));
  const result = await callApi("/api/preview-email", {
    method: "POST",
    body: JSON.stringify(payload)
  });
  await setPendingPreview({ payload, events: result.events || [] });
  return {
    ...result,
    detected: {
      subject: email.subject || "",
      sender: email.sender || ""
    }
  };
}

export async function confirmCurrentPreview(reviewedEvents = null) {
  const preview = await getPendingPreview();
  const events = Array.isArray(reviewedEvents) ? reviewedEvents : preview?.events;
  if (!preview || !events?.length) {
    throw new Error("Extract an event and review it before saving.");
  }
  await setPendingPreview({ ...preview, events });
  const result = await callApi("/api/confirm-extraction", {
    method: "POST",
    body: JSON.stringify({
      metadata: preview.payload.metadata,
      email_hash: preview.payload.email_hash,
      events
    })
  });
  if (["success", "already_processed"].includes(result.status)) {
    await rememberHash(preview.payload.email_hash);
  }
  await rememberRecent(result.events);
  await sendHeartbeat().catch(() => {});
  await setPendingPreview(null);
  return result;
}

export async function processEmail(email, force = false) {
  const { token } = await apiConfig();
  if (!token) throw new Error("Connect your Miyad account first.");

  const emailHash = await sha256(emailFingerprintSource(email));
  if (!force && (await wasProcessed(emailHash))) {
    return { status: "already_processed", events_created: 0 };
  }
  const payload = buildEmailPayload(email);
  payload.email_hash = emailHash;

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
        "https://outlook.live.com/*",
        "https://outlook.office365.com/*",
        "https://outlook.cloud.microsoft/*"
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
      case "EMAIL_DETECTED":
        currentEmail = message.email?.body ? message.email : null;
        await setPendingPreview(null);
        return { detected: Boolean(currentEmail) };
      case "PREVIEW_CURRENT_EMAIL":
        return previewCurrentEmail();
      case "CONFIRM_CURRENT_PREVIEW":
        return confirmCurrentPreview(message.events);
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
        await accountLanguage();
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
        await accountLanguage();
        return result;
      }
      case "LOGOUT":
        await chrome.storage.sync.remove([STORAGE.TOKEN, STORAGE.USER]);
        return { success: true };
      case "GET_ALERTS":
        return callApi("/api/alerts?status_filter=" + (message.status || "pending"));
      case "RESOLVE_ALERT":
        return callApi(`/api/alerts/${message.alertId}/resolve`, {
          method: "POST",
          body: JSON.stringify(message.payload)
        });
      case "GET_COURSES":
        return callApi("/api/courses");
      case "CREATE_COURSE":
        return callApi("/api/courses", {
          method: "POST",
          body: JSON.stringify(message.payload)
        });
      case "DELETE_COURSE":
        return callApi(`/api/courses/${message.courseCode}`, {
          method: "DELETE"
        });
      case "GET_SCHEDULE":
        return callApi("/api/schedule");
      case "CREATE_SCHEDULE":
        return callApi("/api/schedule", {
          method: "POST",
          body: JSON.stringify(message.payload)
        });
      case "DELETE_SCHEDULE":
        return callApi(`/api/schedule/${message.slotId}`, {
          method: "DELETE"
        });
      case "STATUS": {
        const sync = await syncGet([STORAGE.TOKEN, STORAGE.USER, STORAGE.API_URL]);
        const backend = await checkBackend();
        const detected = await currentOutlookEmail();
        if (sync[STORAGE.TOKEN] && backend.reachable) {
          await sendHeartbeat().catch(() => {});
        }
        const local = await localGet([STORAGE.RECENT, STORAGE.QUEUE]);
        const preferences = await getUiPreferences();
        return {
          connected: Boolean(sync[STORAGE.TOKEN]),
          backend,
          user: sync[STORAGE.USER] || null,
          apiUrl: sync[STORAGE.API_URL] || DEFAULT_API_URL,
          recent: local[STORAGE.RECENT] || [],
          queued: (local[STORAGE.QUEUE] || []).length,
          detectedEmail: detected ? {
            subject: detected.subject || "",
            sender: detected.sender || ""
          } : null,
          preferences
        };
      }
      case "UI_PREFERENCES":
        return getUiPreferences();
      case "SET_THEME": {
        const theme = normalizeTheme(message.theme);
        await chrome.storage.sync.set({ [STORAGE.THEME]: theme });
        return { theme };
      }
      case "SET_LANGUAGE": {
        const language = normalizeLanguage(message.language);
        const values = await syncGet([STORAGE.TOKEN, STORAGE.USER]);
        const updates = { [STORAGE.LANGUAGE]: language };
        if (values[STORAGE.USER]) {
          updates[STORAGE.USER] = {
            ...values[STORAGE.USER],
            preferred_language: language
          };
        }
        await chrome.storage.sync.set(updates);
        if (values[STORAGE.TOKEN]) {
          await callApi("/api/settings", {
            method: "PATCH",
            body: JSON.stringify({ preferred_language: language })
          });
        }
        return { language };
      }
      case "SET_API_URL":
        await chrome.storage.sync.set({
          [STORAGE.API_URL]: normalizeApiUrl(message.apiUrl)
        });
        return checkBackend();
      default:
        throw new Error("Unknown message");
    }
  };
  respond()
    .then((data) => sendResponse({ ok: true, data }))
    .catch((error) => sendResponse({ ok: false, error: error.message }));
  return true;
});
