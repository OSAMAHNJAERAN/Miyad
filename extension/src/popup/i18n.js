export const dictionaries = {
  en: {
    appName: "Miyad",
    tagline: "Academic schedule assistant",
    themeLabel: "Theme",
    languageLabel: "Language",
    themeSystem: "System",
    themeLight: "Light",
    themeDark: "Dark",
    loading: "Loading your schedule...",
    login: "Sign in",
    register: "Create account",
    name: "Name",
    namePlaceholder: "Your name",
    email: "Email",
    password: "Password",
    university: "University",
    universityPlaceholder: "University name",
    connect: "Connect to Miyad",
    createAccount: "Create account",
    connectedTitle: "Connected and monitoring Outlook",
    privacyNote: "Miyad reads only the email you open. Your university password is never requested.",
    recentEvents: "Recent events",
    logout: "Log out",
    serverSettings: "Server settings",
    backendUrl: "Backend URL",
    save: "Save",
    testConnection: "Test connection",
    retry: "Retry",
    serverOnline: "Server connected",
    serverOffline: "Server unavailable",
    serverOfflineHelp: "Start FastAPI or enter the correct deployed backend URL.",
    emptyRecent: "Open an academic email in Outlook to begin extraction.",
    queuedOne: "1 queued retry",
    queuedMany: "{count} queued retries",
    eventExam: "Exam",
    eventDeadline: "Deadline",
    eventQuiz: "Quiz",
    eventLecture: "Lecture",
    eventOther: "Event",
    authenticating: "Connecting...",
    saved: "Saved",
    permissionDenied: "Permission to access this backend was not granted.",
    invalidBackend: "Backend URL must use HTTP or HTTPS.",
    unreachableAuth: "The backend is unavailable. Check the server URL and retry.",
    unexpectedError: "Unexpected error",
    accountSync: "Language is synced with your Miyad account.",
    timelineCaptured: "Captured",
  },
  ar: {
    appName: "مِيعاد",
    tagline: "مساعد الجدول الأكاديمي",
    themeLabel: "المظهر",
    languageLabel: "اللغة",
    themeSystem: "النظام",
    themeLight: "فاتح",
    themeDark: "داكن",
    loading: "جاري تحميل جدولك...",
    login: "دخول",
    register: "حساب جديد",
    name: "الاسم",
    namePlaceholder: "اسمك",
    email: "البريد الإلكتروني",
    password: "كلمة المرور",
    university: "الجامعة",
    universityPlaceholder: "اسم الجامعة",
    connect: "الاتصال بمِيعاد",
    createAccount: "إنشاء الحساب",
    connectedTitle: "متصل ويراقب Outlook",
    privacyNote: "يقرأ مِيعاد الرسالة التي تفتحها فقط، ولا يطلب كلمة مرور الجامعة.",
    recentEvents: "آخر المواعيد",
    logout: "تسجيل الخروج",
    serverSettings: "إعدادات الخادم",
    backendUrl: "رابط الخادم",
    save: "حفظ",
    testConnection: "اختبار الاتصال",
    retry: "إعادة المحاولة",
    serverOnline: "الخادم متصل",
    serverOffline: "الخادم غير متاح",
    serverOfflineHelp: "شغّل FastAPI أو أدخل رابط النشر الصحيح.",
    emptyRecent: "افتح رسالة أكاديمية في Outlook لبدء الاستخلاص.",
    queuedOne: "محاولة واحدة بالانتظار",
    queuedMany: "{count} محاولات بالانتظار",
    eventExam: "اختبار",
    eventDeadline: "موعد تسليم",
    eventQuiz: "اختبار قصير",
    eventLecture: "محاضرة",
    eventOther: "موعد",
    authenticating: "جارٍ الاتصال...",
    saved: "تم الحفظ",
    permissionDenied: "لم يتم منح الإذن للوصول إلى هذا الخادم.",
    invalidBackend: "يجب أن يستخدم رابط الخادم HTTP أو HTTPS.",
    unreachableAuth: "الخادم غير متاح. تحقق من الرابط ثم أعد المحاولة.",
    unexpectedError: "حدث خطأ غير متوقع",
    accountSync: "تتزامن اللغة مع حسابك في مِيعاد.",
    timelineCaptured: "تم الالتقاط",
  },
};

export function normalizeLanguage(value) {
  const language = String(value || "").toLowerCase().split("-")[0];
  return Object.hasOwn(dictionaries, language) ? language : "en";
}

export function translate(language, key, values = {}) {
  const dictionary = dictionaries[normalizeLanguage(language)];
  const fallback = dictionaries.en[key] || key;
  return String(dictionary[key] || fallback).replace(
    /\{(\w+)\}/g,
    (_, name) => values[name] ?? `{${name}}`
  );
}

export function applyTranslations(root, language) {
  root.querySelectorAll("[data-i18n]").forEach((element) => {
    element.textContent = translate(language, element.dataset.i18n);
  });
  root.querySelectorAll("[data-i18n-placeholder]").forEach((element) => {
    element.placeholder = translate(language, element.dataset.i18nPlaceholder);
  });
  root.querySelectorAll("[data-i18n-aria]").forEach((element) => {
    element.setAttribute(
      "aria-label",
      translate(language, element.dataset.i18nAria)
    );
  });
}
