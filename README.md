# Miyad / ميعاد

Miyad is a bilingual academic schedule assistant. A Manifest V3 Chrome
extension reads the Outlook message currently open by the student, sends it
to a FastAPI backend, converts academic dates into structured events through
OpenRouter, stores those events in Supabase, and displays them in a Kotlin
Jetpack Compose Android app.

## Projects

- `backend/`: FastAPI, JWT authentication, OpenRouter extraction, Supabase SQL.
- `extension/`: zero-build Chrome extension for Outlook Web App.
- `frontend/`: Android app using Compose, Retrofit, MVVM, and encrypted tokens.
- `design.md`: shared visual and interaction system.
- `change.md`: implementation history, verification, and limitations.

## Quick start

### Backend

```powershell
cd backend
python -m venv .venv
.\.venv\Scripts\python -m pip install -r requirements.txt
Copy-Item .env.example .env
.\.venv\Scripts\python -m uvicorn app.main:app --reload
```

The development default uses in-memory storage. For Supabase, apply
`backend/supabase/schema.sql`, set `DATABASE_BACKEND=supabase`, and fill the
Supabase variables in `.env`.

Email extraction does not use a fake template when OpenRouter is missing.
`/health` reports `ai_configured: false`, and extraction endpoints return a
clear configuration error until `OPENROUTER_API_KEY` is set.

### Android

```powershell
cd frontend
java -classpath .\gradle\wrapper\gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain assembleDebug
```

Open `frontend` in Android Studio or install
`frontend/app/build/outputs/apk/debug/app-debug.apk`. The emulator backend URL
defaults to `http://10.0.2.2:8000/`.

From the repository root, `run_app.ps1` can build, install, and launch the app.
It uses `ANDROID_SDK_ROOT` or `ANDROID_HOME`; set `MIYAD_AVD_NAME` when your AVD
is not named `medium_phone`.

### Chrome extension

Open `chrome://extensions`, enable Developer mode, select **Load unpacked**,
and choose the `extension` directory. Open the popup and connect the same
Miyad account used by the mobile app. The popup detects the current Outlook
message, previews extracted details and confidence, and saves only after the
student confirms the review.

## Required production environment

```env
DATABASE_BACKEND=supabase
JWT_SECRET=
SUPABASE_URL=
SUPABASE_SERVICE_KEY=
OPENROUTER_API_KEY=
OPENROUTER_MODEL=google/gemma-2-9b-it:free
OPENROUTER_HTTP_REFERER=https://miyad.app
OPENROUTER_X_TITLE=Miyad Academic Assistant
```

No real keys are committed.
