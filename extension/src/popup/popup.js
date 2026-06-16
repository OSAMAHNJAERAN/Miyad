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
  list.innerHTML = events.map((event, index) => {
    let verificationBox = "";
    if (event.verification_action) {
      const actionClass = event.verification_action; // auto_add, needs_review, not_matching
      const actionLabel = t({
        auto_add: "actionAutoAdd",
        needs_review: "actionNeedsReview",
        not_matching: "actionNotMatching"
      }[event.verification_action] || "actionNeedsReview");
      
      verificationBox = `
        <div class="verification-status-box ${actionClass}">
          <strong>${escapeHtml(t("verificationStatus"))}: ${escapeHtml(actionLabel)}</strong>
          ${event.verification_reason ? `<p style="margin: 4px 0 0; font-size: 10px;">${escapeHtml(t("aiReason"))}: ${escapeHtml(event.verification_reason)}</p>` : ''}
        </div>
      `;
    }

    return `
      <article class="preview-event" data-preview-index="${index}">
        ${verificationBox}
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
    `;
  }).join("");
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

// --- View Tab Routing ---
document.querySelectorAll(".view-tab").forEach((tabButton) => {
  tabButton.addEventListener("click", () => {
    document.querySelectorAll(".view-tab").forEach((btn) => btn.classList.remove("active"));
    tabButton.classList.add("active");
    
    const target = tabButton.dataset.target;
    document.querySelectorAll(".view-section").forEach((section) => {
      section.classList.toggle("hidden", section.id !== target);
    });

    if (target === "alertsSection") {
      loadAlerts();
    } else if (target === "coursesSection") {
      loadCoursesAndSchedule();
    }
  });
});

// --- Alerts Dashboard ---
async function loadAlerts() {
  const alertsList = document.querySelector("#alertsList");
  alertsList.innerHTML = `<div class="skeleton skeleton-line"></div>`;
  try {
    const alerts = await request({ type: "GET_ALERTS", status: "pending" });
    renderAlerts(alerts);
  } catch (error) {
    alertsList.innerHTML = `<p class="error">${escapeHtml(error.message)}</p>`;
  }
}

function renderAlerts(alerts) {
  const alertsList = document.querySelector("#alertsList");
  if (!alerts.length) {
    alertsList.innerHTML = `<div class="empty">${escapeHtml(t("noAlerts"))}</div>`;
    return;
  }
  
  alertsList.innerHTML = alerts.map((alert) => {
    const event = alert.event_data || {};
    const alertType = alert.alert_type; // needs_review, not_matching
    const actionLabel = t({
      needs_review: "actionNeedsReview",
      not_matching: "actionNotMatching"
    }[alertType] || "actionNeedsReview");

    return `
      <article class="glass alert-item" data-alert-id="${alert.id}">
        <div class="alert-header">
          <h3>${escapeHtml(event.title || t("eventOther"))}</h3>
          <span class="alert-badge ${alertType}">${escapeHtml(actionLabel)}</span>
        </div>
        ${alert.ai_reason ? `<div class="alert-reason"><strong>${escapeHtml(t("aiReason"))}:</strong> ${escapeHtml(alert.ai_reason)}</div>` : ''}
        
        <div class="alert-edit-grid">
          <label>
            <span>${escapeHtml(t("title"))}</span>
            <input class="alert-field" data-field="title" value="${escapeHtml(event.title || '')}">
          </label>
          <label>
            <span>${escapeHtml(t("eventType"))}</span>
            <select class="alert-field" data-field="event_type">
              <option value="exam" ${event.event_type === 'exam' ? 'selected' : ''}>${escapeHtml(t("eventExam"))}</option>
              <option value="deadline" ${event.event_type === 'deadline' ? 'selected' : ''}>${escapeHtml(t("eventDeadline"))}</option>
              <option value="quiz" ${event.event_type === 'quiz' ? 'selected' : ''}>${escapeHtml(t("eventQuiz"))}</option>
              <option value="lecture" ${event.event_type === 'lecture' ? 'selected' : ''}>${escapeHtml(t("eventLecture"))}</option>
              <option value="other" ${event.event_type === 'other' ? 'selected' : ''}>${escapeHtml(t("eventOther"))}</option>
            </select>
          </label>
          <label>
            <span>${escapeHtml(t("date"))}</span>
            <input class="alert-field" data-field="due_date" value="${escapeHtml(event.due_date || '')}">
          </label>
          <label>
            <span>${escapeHtml(t("course"))}</span>
            <input class="alert-field" data-field="course_name" value="${escapeHtml(event.course_name || event.course_code || '')}">
          </label>
          <label style="grid-column: 1 / -1;">
            <span>${escapeHtml(t("location"))}</span>
            <input class="alert-field" data-field="location" value="${escapeHtml(event.location || '')}">
          </label>
          <label style="grid-column: 1 / -1;">
            <span>${escapeHtml(t("notes"))}</span>
            <input class="alert-field" data-field="notes" value="${escapeHtml(event.notes || '')}">
          </label>
        </div>

        <div class="alert-actions">
          <button class="secondary btn-reject" type="button" data-action="reject">${escapeHtml(t("reject"))}</button>
          <button class="primary btn-approve" type="button" data-action="confirm">
            <span class="button-label">${escapeHtml(t("approve"))}</span>
            <span class="button-loader" aria-hidden="true"></span>
          </button>
        </div>
      </article>
    `;
  }).join("");

  // Bind actions
  alertsList.querySelectorAll(".alert-item").forEach((item) => {
    const alertId = item.dataset.alertId;
    const btnApprove = item.querySelector('[data-action="confirm"]');
    const btnReject = item.querySelector('[data-action="reject"]');

    btnApprove.addEventListener("click", async () => {
      // Gather edited event data
      const event_data = {};
      item.querySelectorAll(".alert-field").forEach((field) => {
        event_data[field.dataset.field] = field.value;
      });

      // Preserve hidden fields if present in original
      const originalAlert = alerts.find(a => a.id === alertId);
      const originalEvent = originalAlert?.event_data || {};
      event_data.all_day = originalEvent.all_day || false;
      event_data.confidence = originalEvent.confidence || "medium";

      try {
        setBusy(btnApprove, true);
        await request({
          type: "RESOLVE_ALERT",
          alertId,
          payload: { action: "confirm", event_data }
        });
        globalStatus.textContent = t("alertResolved");
        loadAlerts();
        refresh(); // to refresh recent list
      } catch (err) {
        globalStatus.textContent = err.message;
      } finally {
        setBusy(btnApprove, false);
      }
    });

    btnReject.addEventListener("click", async () => {
      try {
        setBusy(btnReject, true);
        await request({
          type: "RESOLVE_ALERT",
          alertId,
          payload: { action: "reject" }
        });
        globalStatus.textContent = t("alertResolved");
        loadAlerts();
      } catch (err) {
        globalStatus.textContent = err.message;
      } finally {
        setBusy(btnReject, false);
      }
    });
  });
}

// --- Courses & Schedule Dashboard ---
async function loadCoursesAndSchedule() {
  const coursesList = document.querySelector("#coursesList");
  const scheduleList = document.querySelector("#scheduleList");
  coursesList.innerHTML = `<div class="skeleton skeleton-line"></div>`;
  scheduleList.innerHTML = `<div class="skeleton skeleton-line"></div>`;
  
  try {
    const [courses, schedule] = await Promise.all([
      request({ type: "GET_COURSES" }),
      request({ type: "GET_SCHEDULE" })
    ]);
    
    renderCourses(courses);
    renderSchedule(schedule);
    populateCourseDropdown(courses);
  } catch (error) {
    coursesList.innerHTML = `<p class="error">${escapeHtml(error.message)}</p>`;
    scheduleList.innerHTML = `<p class="error">${escapeHtml(error.message)}</p>`;
  }
}

function renderCourses(courses) {
  const coursesList = document.querySelector("#coursesList");
  if (!courses.length) {
    coursesList.innerHTML = `<div class="empty">${escapeHtml(t("noCourses"))}</div>`;
    return;
  }
  
  coursesList.innerHTML = courses.map((course) => `
    <div class="course-row">
      <div class="course-info">
        <h3>${escapeHtml(course.course_code)}</h3>
        <p>${escapeHtml(course.course_name)}</p>
        ${course.teaching_plan ? `<p style="font-size: 10px; font-style: italic;">${escapeHtml(course.teaching_plan)}</p>` : ''}
      </div>
      <button class="btn-delete" data-code="${escapeHtml(course.course_code)}">${escapeHtml(t("delete"))}</button>
    </div>
  `).join("");

  coursesList.querySelectorAll(".btn-delete").forEach((btn) => {
    btn.addEventListener("click", async () => {
      const courseCode = btn.dataset.code;
      try {
        btn.disabled = true;
        await request({ type: "DELETE_COURSE", courseCode });
        loadCoursesAndSchedule();
      } catch (err) {
        globalStatus.textContent = err.message;
      }
    });
  });
}

function renderSchedule(slots) {
  const scheduleList = document.querySelector("#scheduleList");
  if (!slots.length) {
    scheduleList.innerHTML = `<div class="empty">${escapeHtml(t("noSlots"))}</div>`;
    return;
  }

  scheduleList.innerHTML = slots.map((slot) => `
    <div class="schedule-row">
      <div class="schedule-info">
        <h3>${escapeHtml(slot.course_code)}</h3>
        <p>${escapeHtml(t(slot.day_of_week))} · ${escapeHtml(slot.start_time.substring(0, 5))} - ${escapeHtml(slot.end_time.substring(0, 5))}</p>
        ${slot.location ? `<p style="font-size: 10px;">${escapeHtml(slot.location)}</p>` : ''}
      </div>
      <button class="btn-delete" data-id="${escapeHtml(slot.id)}">${escapeHtml(t("delete"))}</button>
    </div>
  `).join("");

  scheduleList.querySelectorAll(".btn-delete").forEach((btn) => {
    btn.addEventListener("click", async () => {
      const slotId = btn.dataset.id;
      try {
        btn.disabled = true;
        await request({ type: "DELETE_SCHEDULE", slotId });
        loadCoursesAndSchedule();
      } catch (err) {
        globalStatus.textContent = err.message;
      }
    });
  });
}

function populateCourseDropdown(courses) {
  const select = document.querySelector("#scheduleCourseCode");
  select.innerHTML = courses.map(c => `
    <option value="${escapeHtml(c.course_code)}">${escapeHtml(c.course_code)} - ${escapeHtml(c.course_name)}</option>
  `).join("");
}

// Course Form Submit
document.querySelector("#courseForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const errorEl = document.querySelector("#courseError");
  errorEl.textContent = "";
  const code = document.querySelector("#courseCode").value.trim().toUpperCase();
  const name = document.querySelector("#courseName").value.trim();
  const plan = document.querySelector("#teachingPlan").value.trim();

  if (!code || !name) return;
  
  const submitBtn = document.querySelector("#courseSubmit");
  try {
    setBusy(submitBtn, true);
    await request({
      type: "CREATE_COURSE",
      payload: { course_code: code, course_name: name, teaching_plan: plan || null }
    });
    document.querySelector("#courseCode").value = "";
    document.querySelector("#courseName").value = "";
    document.querySelector("#teachingPlan").value = "";
    loadCoursesAndSchedule();
  } catch (err) {
    errorEl.textContent = err.message;
  } finally {
    setBusy(submitBtn, false);
  }
});

// Schedule Form Submit
document.querySelector("#scheduleForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const errorEl = document.querySelector("#scheduleError");
  errorEl.textContent = "";
  const code = document.querySelector("#scheduleCourseCode").value;
  const day = document.querySelector("#scheduleDayOfWeek").value;
  const start = document.querySelector("#scheduleStartTime").value;
  const end = document.querySelector("#scheduleEndTime").value;
  const loc = document.querySelector("#scheduleLocation").value.trim();

  if (!code || !day || !start || !end) return;

  const submitBtn = document.querySelector("#scheduleSubmit");
  try {
    setBusy(submitBtn, true);
    await request({
      type: "CREATE_SCHEDULE",
      payload: {
        course_code: code,
        day_of_week: day,
        start_time: start + ":00",
        end_time: end + ":00",
        location: loc || null
      }
    });
    document.querySelector("#scheduleStartTime").value = "";
    document.querySelector("#scheduleEndTime").value = "";
    document.querySelector("#scheduleLocation").value = "";
    loadCoursesAndSchedule();
  } catch (err) {
    errorEl.textContent = err.message;
  } finally {
    setBusy(submitBtn, false);
  }
});


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
