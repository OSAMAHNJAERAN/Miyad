const views = {
  loading: document.querySelector("#loadingView"),
  auth: document.querySelector("#authView"),
  connected: document.querySelector("#connectedView")
};
const authForm = document.querySelector("#authForm");
const authError = document.querySelector("#authError");
const nameField = document.querySelector("#nameField");
const universityField = document.querySelector("#universityField");
let mode = "login";

function request(message) {
  return new Promise((resolve, reject) => {
    chrome.runtime.sendMessage(message, (response) => {
      if (chrome.runtime.lastError) {
        reject(new Error(chrome.runtime.lastError.message));
      } else if (!response?.ok) {
        reject(new Error(response?.error || "Unexpected error"));
      } else {
        resolve(response.data);
      }
    });
  });
}

function show(name) {
  Object.entries(views).forEach(([key, view]) => {
    view.classList.toggle("hidden", key !== name);
  });
}

function originPattern(value) {
  const url = new URL(value);
  if (!["http:", "https:"].includes(url.protocol)) {
    throw new Error("Backend URL must use HTTP or HTTPS.");
  }
  return `${url.origin}/*`;
}

async function ensureApiPermission(apiUrl) {
  const origins = [originPattern(apiUrl)];
  const granted = await chrome.permissions.request({ origins });
  if (!granted) throw new Error("Permission to access this backend was not granted.");
}

function formatEventType(type) {
  return {
    exam: "اختبار",
    deadline: "موعد تسليم",
    quiz: "اختبار قصير",
    lecture: "محاضرة",
    other: "موعد"
  }[type] || "موعد";
}

function renderRecent(items) {
  const list = document.querySelector("#recentList");
  if (!items.length) {
    list.innerHTML = '<div class="empty">افتح رسالة أكاديمية في Outlook لبدء الاستخلاص.</div>';
    return;
  }
  list.innerHTML = items.slice(0, 4).map((item) => `
    <article class="recent-item">
      <div class="recent-icon">✓</div>
      <div>
        <strong>${escapeHtml(item.title)}</strong>
        <small>${formatEventType(item.event_type)} · ${new Date(item.due_date).toLocaleString()}</small>
      </div>
    </article>
  `).join("");
}

function escapeHtml(value) {
  const element = document.createElement("span");
  element.textContent = value || "";
  return element.innerHTML;
}

async function refresh() {
  show("loading");
  try {
    const status = await request({ type: "STATUS" });
    document.querySelector("#apiUrl").value = status.apiUrl;
    if (!status.connected) {
      show("auth");
      return;
    }
    document.querySelector("#userEmail").textContent = status.user?.email || "";
    document.querySelector("#queueBadge").textContent =
      status.queued ? `${status.queued} بانتظار إعادة المحاولة` : "";
    renderRecent(status.recent);
    show("connected");
  } catch (error) {
    authError.textContent = error.message;
    show("auth");
  }
}

document.querySelectorAll(".tab").forEach((tab) => {
  tab.addEventListener("click", () => {
    mode = tab.dataset.mode;
    document.querySelectorAll(".tab").forEach((item) => {
      item.classList.toggle("active", item === tab);
    });
    const registering = mode === "register";
    nameField.classList.toggle("hidden", !registering);
    universityField.classList.toggle("hidden", !registering);
    document.querySelector("#name").required = registering;
    document.querySelector("#university").required = registering;
  });
});

authForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  authError.textContent = "";
  const payload = {
    email: document.querySelector("#email").value.trim(),
    password: document.querySelector("#password").value
  };
  if (mode === "register") {
    payload.name = document.querySelector("#name").value.trim();
    payload.university = document.querySelector("#university").value.trim();
  }
  try {
    await request({ type: mode === "register" ? "REGISTER" : "LOGIN", payload });
    await refresh();
  } catch (error) {
    authError.textContent = error.message;
  }
});

document.querySelector("#logoutButton").addEventListener("click", async () => {
  await request({ type: "LOGOUT" });
  await refresh();
});

document.querySelector("#saveApiUrl").addEventListener("click", async () => {
  const apiError = document.querySelector("#apiError");
  apiError.textContent = "";
  const apiUrl = document.querySelector("#apiUrl").value.trim();
  try {
    await ensureApiPermission(apiUrl);
    await request({ type: "SET_API_URL", apiUrl });
    await refresh();
  } catch (error) {
    apiError.textContent = error.message;
  }
});

refresh();
