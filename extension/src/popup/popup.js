import {
  applyTranslations,
  normalizeLanguage,
  translate
} from "./i18n.js";

const views = {
  loading: document.querySelector("#loadingView"),
  auth: document.querySelector("#authView"),
  connected: document.querySelector("#connectedView")
};
const authForm = document.querySelector("#authForm");
const authError = document.querySelector("#authError");
const authSubmit = document.querySelector("#authSubmit");
const nameField = document.querySelector("#nameField");
const universityField = document.querySelector("#universityField");
const themeSelect = document.querySelector("#themeSelect");
const languageSelect = document.querySelector("#languageSelect");
const globalStatus = document.querySelector("#globalStatus");
let mode = "login";
let language = "en";
let lastStatus = null;
let previewEvents = [];

function t(key, values) {
  return translate(language, key, values);
}

function request(message) {
  return new Promise((resolve, reject) => {
    chrome.runtime.sendMessage(message, (response) => {
      if (chrome.runtime.lastError) {
        reject(new Error(chrome.runtime.lastError.message));
      } else if (!response?.ok) {
        reject(new Error(response?.error || t("unexpectedError")));
      } else {
        resolve(response.data);
      }
    });
  });
}

function friendlyExtractionError(error) {
  if (/OPENROUTER_API_KEY|AI extraction is not configured/i.test(error.message)) {
    return t("aiNotConfigured");
  }
  return error.message;
}

function show(name) {
  Object.entries(views).forEach(([key, view]) => {
    view.classList.toggle("hidden", key !== name);
  });
}

function applyTheme(theme) {
  const normalized = ["system", "light", "dark"].includes(theme)
    ? theme
    : "system";
  document.documentElement.dataset.theme = normalized;
  themeSelect.value = normalized;
}

function applyLanguage(nextLanguage) {
  language = normalizeLanguage(nextLanguage);
  document.documentElement.lang = language;
  document.documentElement.dir = language === "ar" ? "rtl" : "ltr";
  languageSelect.value = language;
  applyTranslations(document, language);
  updateAuthButton();
  if (lastStatus) {
    renderConnection(lastStatus.backend);
    renderDetectedEmail(lastStatus.detectedEmail);
    renderRecent(lastStatus.recent || []);
    renderQueue(lastStatus.queued || 0);
  }
  if (previewEvents.length) renderPreview(previewEvents);
}

function updateAuthButton() {
  authSubmit.querySelector(".button-label").textContent =
    mode === "register" ? t("createAccount") : t("connect");
}

function setBusy(button, busy, statusKey = "") {
  button.disabled = busy;
  button.classList.toggle("loading", busy);
  if (statusKey) {
    globalStatus.textContent = busy ? t(statusKey) : "";
  }
}

function originPattern(value) {
  const url = new URL(value);
  if (!["http:", "https:"].includes(url.protocol)) {
    throw new Error(t("invalidBackend"));
  }
  return `${url.origin}/*`;
}

async function ensureApiPermission(apiUrl) {
  const granted = await chrome.permissions.request({
    origins: [originPattern(apiUrl)]
  });
  if (!granted) throw new Error(t("permissionDenied"));
}

function formatEventType(type) {
  return t({
    exam: "eventExam",
    deadline: "eventDeadline",
    quiz: "eventQuiz",
    lecture: "eventLecture",
    other: "eventOther"
  }[type] || "eventOther");
}

function renderRecent(items) {
  const list = document.querySelector("#recentList");
  if (!items.length) {
    list.innerHTML = `<div class="empty">${escapeHtml(t("emptyRecent"))}</div>`;
    return;
  }
  const locale = language === "ar" ? "ar" : "en";
  list.innerHTML = items.slice(0, 5).map((item) => `
    <article class="recent-item">
      <div class="recent-icon">
        <img src="../../icons/event.svg" alt="">
      </div>
      <div>
        <strong>${escapeHtml(item.title)}</strong>
        <small>${escapeHtml(formatEventType(item.event_type))} · ${escapeHtml(
          new Date(item.due_date).toLocaleString(locale, {
            dateStyle: "medium",
            timeStyle: "short"
          })
        )}</small>
      </div>
    </article>
  `).join("");
}

function renderQueue(count) {
  document.querySelector("#queueBadge").textContent = count
    ? t(count === 1 ? "queuedOne" : "queuedMany", { count })
    : "";
}

function renderDetectedEmail(email) {
  const subject = document.querySelector("#detectedSubject");
  const sender = document.querySelector("#detectedSender");
  const badge = document.querySelector("#detectionBadge");
  const extractButton = document.querySelector("#extractButton");
  const detected = Boolean(email);
  subject.textContent = detected ? email.subject || t("detectedEmailTitle") : t("noEmailDetected");
  sender.textContent = detected ? email.sender || "" : "";
  badge.textContent = t(detected ? "detected" : "notDetected");
  badge.classList.toggle("active", detected);
  extractButton.disabled = !detected || !lastStatus?.backend?.reachable;
}

function confidenceLabel(value) {
  return t({
    high: "confidenceHigh",
    medium: "confidenceMedium",
    low: "confidenceLow"
  }[value] || "confidenceMedium");
}

function renderPreview(events) {
  previewEvents = events;
  const card = document.querySelector("#previewCard");
  const list = document.querySelector("#previewList");
  const confidence = document.querySelector("#previewConfidence");
  const warning = document.querySelector("#reviewWarning");
  if (!events.length) {
    card.classList.add("hidden");
    list.replaceChildren();
    return;
  }
  const lowest = events.some((event) => event.confidence === "low")
    ? "low"
    : events.some((event) => event.confidence === "medium") ? "medium" : "high";
  confidence.className = `confidence ${lowest}`;
  confidence.textContent = `${t("confidence")}: ${confidenceLabel(lowest)}`;
  warning.classList.toggle("hidden", lowest !== "low");
  list.innerHTML = events.map((event, index) => `
    <article class="preview-event" data-preview-index="${index}">
      <p class="preview-hint">${escapeHtml(t("editBeforeSaving"))}</p>
      <div class="preview-grid">
        ${previewInput("title", t("title"), event.title)}
        ${previewSelect("event_type", t("eventType"), event.event_type)}
        ${previewInput("due_date", t("date"), event.due_date)}
        ${previewInput(
          "course_name",
          t("course"),
          event.course_name || event.course_code || ""
        )}
        ${previewInput("location", t("location"), event.location || "")}
        ${previewInput("notes", t("notes"), event.notes || "")}
      </div>
      <dl class="preview-evidence">
        <dt>${escapeHtml(t("sender"))}</dt>
        <dd>${escapeHtml(event.source_email_sender || "-")}</dd>
        <dt>${escapeHtml(t("evidence"))}</dt>
        <dd>${escapeHtml(event.evidence || "-")}</dd>
      </dl>
    </article>
  `).join("");
  list.querySelectorAll("[data-preview-field]").forEach((control) => {
    const update = () => {
      const index = Number(control.closest(".preview-event").dataset.previewIndex);
      previewEvents[index] = {
        ...previewEvents[index],
        [control.dataset.previewField]: control.value
      };
    };
    control.addEventListener("input", update);
    control.addEventListener("change", update);
  });
  card.classList.remove("hidden");
}

function previewInput(field, label, value) {
  return `
    <label class="preview-field">
      <span>${escapeHtml(label)}</span>
      <input data-preview-field="${field}" value="${escapeHtml(value)}">
    </label>
  `;
}

function previewSelect(field, label, selected) {
  const options = ["exam", "deadline", "quiz", "lecture", "other"];
  return `
    <label class="preview-field">
      <span>${escapeHtml(label)}</span>
      <select data-preview-field="${field}">
        ${options.map((value) => `
          <option value="${value}" ${value === selected ? "selected" : ""}>
            ${escapeHtml(formatEventType(value))}
          </option>
        `).join("")}
      </select>
    </label>
  `;
}

function escapeHtml(value) {
  const element = document.createElement("span");
  element.textContent = value || "";
  return element.innerHTML;
}

function renderConnection(backend) {
  const element = document.querySelector("#connectionStatus");
  const retryButton = document.querySelector("#retryButton");
  if (!backend) {
    element.textContent = "";
    element.className = "connection-status hidden";
    retryButton.classList.add("hidden");
    return;
  }
  element.classList.remove("hidden", "online", "offline");
  element.classList.add(backend.reachable ? "online" : "offline");
  element.textContent = backend.reachable
    ? `${t("serverOnline")} · ${backend.apiUrl}${
      backend.aiConfigured === false ? ` · ${t("aiNotConfigured")}` : ""
    }`
    : `${t("serverOffline")} · ${backend.apiUrl}. ${t("serverOfflineHelp")}`;
  retryButton.classList.toggle("hidden", backend.reachable);
}

async function refresh() {
  show("loading");
  try {
    const status = await request({ type: "STATUS" });
    lastStatus = status;
    applyTheme(status.preferences?.theme || themeSelect.value);
    applyLanguage(status.preferences?.language || language);
    document.querySelector("#apiUrl").value = status.apiUrl;
    renderConnection(status.backend);
    if (!status.connected) {
      show("auth");
      return;
    }
    document.querySelector("#userEmail").textContent = status.user?.email || "";
    renderDetectedEmail(status.detectedEmail);
    renderQueue(status.queued || 0);
    renderRecent(status.recent || []);
    show("connected");
  } catch (error) {
    authError.textContent = error.message;
    show("auth");
  }
}

function setMode(nextMode) {
  mode = nextMode;
  document.querySelectorAll(".tab").forEach((item) => {
    const active = item.dataset.mode === mode;
    item.classList.toggle("active", active);
    item.setAttribute("aria-selected", String(active));
  });
  const registering = mode === "register";
  nameField.classList.toggle("hidden", !registering);
  universityField.classList.toggle("hidden", !registering);
  document.querySelector("#name").required = registering;
  document.querySelector("#university").required = registering;
  document.querySelector("#password").autocomplete =
    registering ? "new-password" : "current-password";
  updateAuthButton();
}

function clearFieldErrors() {
  authForm.querySelectorAll("input").forEach((input) => {
    input.removeAttribute("aria-invalid");
    input.closest(".field")?.querySelector(".field-error")?.replaceChildren();
  });
}

function validateForm() {
  clearFieldErrors();
  if (authForm.checkValidity()) return true;
  authForm.querySelectorAll("input:invalid").forEach((input) => {
    input.setAttribute("aria-invalid", "true");
    const error = input.closest(".field")?.querySelector(".field-error");
    if (error) error.textContent = input.validationMessage;
  });
  authForm.querySelector("input:invalid")?.focus();
  return false;
}

document.querySelectorAll(".tab").forEach((tab) => {
  tab.addEventListener("click", () => setMode(tab.dataset.mode));
});

authForm.addEventListener("input", (event) => {
  event.target.removeAttribute("aria-invalid");
  event.target.closest(".field")?.querySelector(".field-error")?.replaceChildren();
});

authForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  authError.textContent = "";
  if (!validateForm()) return;
  const payload = {
    email: document.querySelector("#email").value.trim(),
    password: document.querySelector("#password").value
  };
  if (mode === "register") {
    payload.name = document.querySelector("#name").value.trim();
    payload.university = document.querySelector("#university").value.trim();
  }
  try {
    if (!lastStatus?.backend?.reachable) throw new Error(t("unreachableAuth"));
    setBusy(authSubmit, true, "authenticating");
    await request({ type: mode === "register" ? "REGISTER" : "LOGIN", payload });
    await refresh();
  } catch (error) {
    authError.textContent = error.message;
  } finally {
    setBusy(authSubmit, false);
  }
});

document.querySelector("#logoutButton").addEventListener("click", async () => {
  await request({ type: "LOGOUT" });
  await refresh();
});

document.querySelector("#extractButton").addEventListener("click", async () => {
  const button = document.querySelector("#extractButton");
  const error = document.querySelector("#extractError");
  error.textContent = "";
  renderPreview([]);
  try {
    setBusy(button, true, "extracting");
    const result = await request({ type: "PREVIEW_CURRENT_EMAIL" });
    if (!result.events?.length) {
      error.textContent = t("noEventFound");
      return;
    }
    renderPreview(result.events);
  } catch (reason) {
    error.textContent = friendlyExtractionError(reason);
  } finally {
    setBusy(button, false);
  }
});

document.querySelector("#cancelPreview").addEventListener("click", () => {
  renderPreview([]);
  document.querySelector("#extractError").textContent = "";
});

document.querySelector("#confirmPreview").addEventListener("click", async () => {
  const button = document.querySelector("#confirmPreview");
  const error = document.querySelector("#extractError");
  error.textContent = "";
  try {
    const invalid = previewEvents.some((event) =>
      !event.title?.trim() || Number.isNaN(Date.parse(event.due_date))
    );
    if (invalid) throw new Error(t("reviewRequiredFields"));
    setBusy(button, true, "sending");
    await request({ type: "CONFIRM_CURRENT_PREVIEW", events: previewEvents });
    renderPreview([]);
    globalStatus.textContent = t("sentSuccessfully");
    await refresh();
  } catch (reason) {
    error.textContent = friendlyExtractionError(reason);
  } finally {
    setBusy(button, false);
  }
});

document.querySelector("#retryButton").addEventListener("click", refresh);

themeSelect.addEventListener("change", async () => {
  applyTheme(themeSelect.value);
  await request({ type: "SET_THEME", theme: themeSelect.value });
});

languageSelect.addEventListener("change", async () => {
  applyLanguage(languageSelect.value);
  try {
    await request({ type: "SET_LANGUAGE", language });
  } catch (error) {
    authError.textContent = error.message;
  }
});

async function saveApiUrl(shouldRefresh) {
  const apiError = document.querySelector("#apiError");
  apiError.textContent = "";
  const apiUrl = document.querySelector("#apiUrl").value.trim();
  try {
    await ensureApiPermission(apiUrl);
    const backend = await request({ type: "SET_API_URL", apiUrl });
    lastStatus = { ...(lastStatus || {}), apiUrl: backend.apiUrl, backend };
    renderConnection(backend);
    if (!backend.reachable) throw new Error(backend.error || t("serverOfflineHelp"));
    globalStatus.textContent = t("saved");
    if (shouldRefresh) await refresh();
  } catch (error) {
    apiError.textContent = error.message;
  }
}

document.querySelector("#saveApiUrl").addEventListener("click", () => saveApiUrl(true));
document.querySelector("#testApiUrl").addEventListener("click", () => saveApiUrl(false));

async function initialize() {
  try {
    const preferences = await request({ type: "UI_PREFERENCES" });
    const browserLanguage = normalizeLanguage(navigator.language);
    const initialLanguage = preferences.hasStoredLanguage
      ? preferences.language
      : browserLanguage;
    applyTheme(preferences.theme);
    applyLanguage(initialLanguage);
    if (!preferences.hasStoredLanguage) {
      await request({ type: "SET_LANGUAGE", language: initialLanguage });
    }
  } catch {
    applyTheme("system");
    applyLanguage(navigator.language);
  }
  setMode("login");
  await refresh();
}

initialize();
