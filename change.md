# Change Log

## 2026-06-13: Unified Glass Redesign

### Android

- Added encrypted `system`, `light`, and `dark` theme persistence with immediate
  runtime switching.
- Added shared glass background/card/status components and adaptive color tokens.
- Moved body, form, card, and navigation typography to Thmanyah Sans.
- Redesigned authentication with a glass panel, password visibility, keyboard
  insets, and adaptive contrast.
- Added keyboard-aware floating navigation and retained the five-position model
  with the central Add action.
- Replaced Add Event text date/time entry with native Material pickers.
- Moved recurrence, reminder, location, description, and color into expandable
  optional details with a fixed save action.
- Added Home quick actions and relabeled progress as a truthful week timeline.
- Added a Canvas event-breakdown chart driven by actual event counts.
- Added a System/Light/Dark selector to Account.
- Split theme, glass components, Add Event, form validation, and statistics into
  dedicated source files while preserving backend payloads.

### Chrome extension

- Rebuilt the popup as a 390px adaptive glass surface with light/dark/system
  themes and solid fallbacks.
- Replaced the CSS mark with the real Miyad logo and local SVG UI icons.
- Added complete Arabic and English dictionaries using `data-i18n` bindings.
- Added browser-locale pre-login language, authenticated account-language sync,
  immediate RTL/LTR switching, and persisted theme/language selectors.
- Added service-worker preference messages using existing `GET/PATCH
  /api/settings`.
- Added skeleton loading, disabled/loading submission, inline validation,
  visible focus states, retry UI, `aria-live` feedback, and reduced-motion CSS.
- Preserved health checks, auth, recent events, retry queue, backend settings,
  Outlook processing, and heartbeat behavior.

### Verification

- Backend: `11 passed`.
- Extension: `22 passed`, including dictionary parity, locale fallback, theme
  persistence, account language synchronization, and popup token checks.
- Android: unit tests, lint, and debug APK assembly passed.
- Android light and dark authentication captures:
  `screenshots/runtime_glass_current.png` and
  `screenshots/runtime_glass_auth_dark.png`.
- Browser screenshot automation was unavailable in the Windows sandbox; static
  extension UI verification is documented in `design-qa.md`.

## 2026-06-13: Event System, Floating Navigation, And Extension Connectivity

### Backend and database

- Added authenticated `POST /api/events` for independent manual calendar events.
- Added description, start time, end time, all-day, repeat, reminder, event color,
  location, and event type fields while preserving `due_date` compatibility for
  AI-extracted events.
- Added timezone and end-after-start validation.
- Extended in-memory and Supabase storage implementations.
- Added idempotent PostgreSQL migration statements and extraction defaults.
- Added API tests for successful event creation and invalid time ranges.

### Android app

- Replaced the four-item dark bar with the supplied floating white pill direction.
- Added Home, Calendar, elevated Add Event, Statistics, and Account destinations.
- Added a connected Add Event sheet with all requested scheduling controls.
- Added Statistics and retained Smart Extraction through a Home action.
- Calendar days now show three independently colored dots and an overflow count.
- Event details now show start/end time, recurrence, and description.
- Added spring navigation feedback, animated form entrance, and native ripple.
- Replaced splash and empty-state artwork with the supplied Miyad logo reference.

### Extension and "Failed to fetch"

- Added an explicit `/health` probe for the configured backend.
- Popup status now distinguishes a reachable backend from an unavailable URL.
- Network and timeout errors include the backend origin and actionable guidance.
- Added a visible connection test beside backend URL configuration.
- Preserved heartbeats, permissions, retry queue, deduplication, and extraction.
- Added healthy/offline backend status tests.

### Visual QA

- Captured runtime states in `screenshots/runtime_latest.png` and
  `screenshots/runtime_add_event.png`.
- Runtime QA found a partially collapsed Add Event sheet; it was fixed by forcing
  the sheet to open fully expanded.
- `design-qa.md` records the comparison and passes.

### Verification

- Backend: `11 passed in 4.16s`.
- Backend `/health`: development, memory database, status `ok`.
- Extension: `15 passed`; JavaScript syntax checks succeeded.
- Android JVM tests, lint, and debug APK assembly succeeded.
- Debug APK: `frontend/app/build/outputs/apk/debug/app-debug.apk`
  (`23,142,942` bytes).
- Emulator install and launch succeeded; no Miyad `AndroidRuntime` crash appeared.

### External production checks still required

- Apply the updated schema to the production Supabase project.
- Supply production Supabase, JWT, CORS, and OpenRouter environment values.
- Set the extension Backend URL to the deployed HTTPS API and grant that origin.
- Test against a signed-in Outlook Web App account.
- Live OpenRouter and production Supabase calls remain credential-dependent.

## 2026-06-11: Connected Product Implementation

### Architecture decision

- Selected the newer `plan.md` architecture: FastAPI + Supabase + OpenRouter,
  Kotlin/Jetpack Compose, and Chrome Extension Manifest V3.
- Did not mix the older Node/Gemini architecture from `Agent.md`.

### Backend files created

- `backend/app/main.py`
- `backend/app/core/config.py`
- `backend/app/core/security.py`
- `backend/app/api/auth.py`
- `backend/app/api/dependencies.py`
- `backend/app/api/email.py`
- `backend/app/api/event.py`
- `backend/app/api/settings.py`
- `backend/app/schemas/auth.py`
- `backend/app/schemas/email.py`
- `backend/app/schemas/event.py`
- `backend/app/schemas/settings.py`
- `backend/app/services/llm.py`
- `backend/app/services/storage.py`
- `backend/tests/test_api.py`
- `backend/supabase/schema.sql`
- `backend/.env.example`
- `backend/requirements.txt`
- `backend/pyproject.toml`
- `backend/Dockerfile`

### Backend behavior

- Added `GET /health`.
- Added register/login with bcrypt password hashing and persistent JWTs.
- Added authenticated `POST /api/process-email`.
- Added `POST /api/extract-manual` with preview (`save=false`) and save modes.
- Added event list, filter, patch, ownership-checked delete.
- Added user settings read/update endpoints.
- Added OpenRouter Gemma extraction with strict Pydantic validation.
- Added one retry when the AI returns malformed JSON.
- Rejected missing-timezone event dates.
- Added in-memory development/test storage and Supabase production storage.
- Raw email content is never passed to persistence methods or stored.
- Remote deduplication uses `(user_id, hash)` to avoid cross-user collisions.

### Database

- Added `users`, `email_hashes`, `events`, `notification_tokens`, and
  `user_settings`.
- Added user/date and hash indexes.
- Added event type, language, and reminder constraints.
- Added composite source-hash ownership foreign key.
- Hash deletion is restricted while referenced; user deletion remains safe because
  both hashes and events are removed by their direct user cascades.

### Chrome extension files created

- `extension/manifest.json`
- `extension/src/content.js`
- `extension/src/background.js`
- `extension/src/popup/popup.html`
- `extension/src/popup/popup.css`
- `extension/src/popup/popup.js`
- `extension/src/utils/hasher.js`
- `extension/src/utils/logger.js`
- `extension/src/utils/scraper.js`
- `extension/icons/icon.svg`
- `extension/icons/icon-16.png`
- `extension/icons/icon-48.png`
- `extension/icons/icon-128.png`
- `extension/fonts/*.woff2`
- `extension/tests/*.test.*`
- `extension/package.json`
- `extension/README.md`

### Extension behavior

- Added MV3 service worker, storage, context menu, alarms, and OWA hosts.
- Added new/classic Outlook fallback selectors.
- URL polling detects navigation; `MutationObserver` attaches only to the
  reading pane and never to `document.body`.
- Extracts subject, sender, timestamp, body, and browser timezone.
- Uses the required SHA-256 input order and a bounded local hash cache.
- Added backend registration/login, connected/disconnected popup states,
  recent events, logout, and configurable backend URL.
- Added manual selected-text context menu extraction.
- Added bounded exponential retry queue for network failures.

### Android files created or replaced

- Added Retrofit/OkHttp API models and repository.
- Added encrypted token/language/onboarding storage.
- Added `AppViewModel` with state flows and coroutine operations.
- Added `ProductApp.kt` with the connected product flow.
- Added `CalendarLogic.kt` with a six-week month matrix and date filtering.
- Added `ReminderPlanner`, `ReminderScheduler`, `ReminderReceiver`, and
  `BootReceiver`.
- Added calendar/reminder JVM unit tests.
- Replaced stock launcher foreground/background with Miyad artwork.
- Removed the duplicate static screen/navigation implementation and stale tests.

### Android screens and states

- Animated splash.
- Four onboarding pages.
- Language selection with Arabic RTL and English LTR.
- Login and registration with loading/error states.
- Dashboard with event counters, upcoming activity, skeletons, empty state,
  refresh, and retry.
- Interactive month calendar with previous/next navigation, adjacent-month
  selection, today/selected states, event dots, and type filters.
- Event details with source, notes, location, per-event reminder override,
  confirmed delete, and Android system-back handling.
- Smart extraction text input, AI loading, preview, no-event state, and save.
- Profile/settings with language, notifications, three reminder timings,
  extension status, privacy note, version, and logout.

### Android reminders

- Schedules local same-day, one-day, and one-week alarms with AlarmManager.
- Merges global preferences with per-event overrides without duplicate reminder
  kinds.
- Requests Android 13+ notification permission and creates a notification
  channel on delivery.
- Persists the reminder plan and restores future alarms after reboot or package
  replacement.
- Cancels alarms on logout and resynchronizes after event/settings changes.

### Design and assets

- Added `design.md`.
- Kept the lime/black/off-white visual identity.
- Used bundled Thmanyah Sans, Serif Display, and Serif Text fonts.
- Added custom extension icons and adaptive Android launcher artwork.
- Used lightweight code-native academic illustrations and icons for onboarding,
  extraction, and empty states.

### Environment variables

- `DATABASE_BACKEND`
- `JWT_SECRET`
- `CORS_ORIGINS`
- `SUPABASE_URL`
- `SUPABASE_SERVICE_KEY`
- `OPENROUTER_API_KEY`
- `OPENROUTER_MODEL`
- `OPENROUTER_HTTP_REFERER`
- `OPENROUTER_X_TITLE`

No real secrets were added.

### Verification performed

- Backend: `3 passed` with FastAPI TestClient.
- Backend runtime: Uvicorn started successfully and `/health` returned
  `{"status":"ok","environment":"development","database":"memory"}`.
- Extension: `3 passed` with Node's test runner.
- Extension manifest JSON and all JavaScript syntax validated.
- Android calendar/reminder logic: `4 passed` with JUnit 4.
- Android: `assembleDebug lintDebug compileDebugUnitTestKotlin` succeeded.
- Android lint: `0 errors`; warnings are dependency-version/style suggestions.
- Debug APK generated at `frontend/app/build/outputs/apk/debug/app-debug.apk`.
- Android emulator cold start, onboarding, English LTR authentication, Arabic
  RTL dashboard, settings, event details, and token persistence were exercised.
- Verified the June/July month grid, adjacent-month navigation, date filtering,
  per-event reminder persistence, global reminder persistence, and AlarmManager
  schedule updates.
- Verified eight future reminders were restored by `BootReceiver` after an
  emulator reboot; Android delivered the boot broadcast 45 seconds after
  `sys.boot_completed`.
- Fixed two issues found during runtime QA: dark button labels inherited the
  wrong content color, and Android Back exited event details instead of
  returning to the originating screen.
- Emulator logcat contained no Miyad `AndroidRuntime` crash.
- Chrome icon PNGs were generated and visually inspected.

### Assumptions

- Production will provide Supabase and OpenRouter credentials.
- Android emulator uses `10.0.2.2` to reach the host backend.
- The extension production API hostname will be `https://api.miyad.app`.
- Thmanyah font use remains governed by the license included in the repository.

### Known limitations

- Live OpenRouter extraction was not executed because no API key was provided;
  the complete pipeline was tested using an injected deterministic extractor.
- Supabase deployment was not performed because no project credentials exist.
- Real Outlook DOM testing requires a signed-in OWA session.
- FCM push delivery is not implemented; Android currently uses local alarms.

## 2026-06-11: Completion Audit Hardening

### Files created

- `.gitignore`
- `backend/tests/test_config.py`
- `backend/tests/test_llm.py`
- `backend/tests/test_supabase_storage.py`
- `extension/tests/background.test.mjs`
- `extension/tests/content.test.mjs`
- `extension/tests/manifest.test.mjs`
- `frontend/app/src/main/java/com/example/miyad/ui/AuthValidation.kt`
- `frontend/app/src/main/java/com/example/miyad/ui/product/DashboardLogic.kt`
- `frontend/app/src/test/java/com/example/miyad/ui/AuthValidationTest.kt`
- `frontend/app/src/test/java/com/example/miyad/ui/product/DashboardLogicTest.kt`

### Files modified

- Backend: `.env.example`, `README.md`, `app/main.py`,
  `app/core/config.py`, `app/api/settings.py`, `app/schemas/email.py`,
  `app/schemas/settings.py`, `app/services/llm.py`,
  `app/services/storage.py`, `supabase/schema.sql`, and `tests/test_api.py`.
- Extension: `README.md`, `manifest.json`, `src/background.js`,
  `src/popup/popup.html`, `src/popup/popup.js`, and
  `tests/background.test.mjs`.
- Android: `README.md`, `data/ApiModels.kt`, `data/MiyadRepository.kt`,
  `data/TokenStore.kt`, `ui/AppViewModel.kt`, and `ui/product/ProductApp.kt`.
- Project log: `change.md`.
- Root tooling/docs: `README.md` and `run_app.ps1`.

### Files deleted

- None. Generated caches were not treated as source changes.

### Backend improvements

- Made Supabase extraction persistence atomic with the `save_extraction`
  PostgreSQL function.
- Enabled row-level security on every Miyad table and restricted extraction RPC
  execution to `service_role`.
- Added production startup validation for Supabase, OpenRouter, JWT, and
  explicit CORS configuration.
- Added an authenticated extension heartbeat and seven-day connection status.
- Added tests for malformed AI retry, invalid timezone dates, Arabic/English
  samples, filters, ownership, production configuration, and the Supabase RPC.
- Added a root ignore policy for secrets, local SDK configuration, virtualenvs,
  bytecode, dependency folders, and build artifacts.

### Extension improvements

- Added optional permissions for user-configured backend origins.
- Validates backend URLs and rejects unsafe schemes or embedded credentials.
- Added a 15-second network timeout.
- Retries only network, rate-limit, and server errors.
- Sends authenticated heartbeats after login/register, status checks, automatic
  captures, manual captures, and successful retries.
- Queues offline context-menu extractions and retries the original manual
  extraction endpoint.
- Expanded tests for token storage, heartbeat auth, context-menu extraction,
  bounded deduplication, retry recovery, and non-retryable errors.
- Added structural regression tests for MV3, required OWA hosts/icons, URL
  polling, and reading-pane-only mutation observation.

### Android improvements

- Added today/current-week summaries, weekly progress, and the `other` counter.
- Added localized client-side authentication validation.
- Preserves the onboarding language during initial account authentication.
- Restores the encrypted user profile with the persistent JWT so cold-start
  greetings and settings identity do not become blank.
- Replaced the hardcoded extension-connected claim with heartbeat status.
- Added staggered event-card entrance motion, calendar selection motion, and
  animated extraction success.
- Reads the displayed app version from `BuildConfig`.
- Made the root Android runner portable across SDK locations and configurable
  AVD names.

### Verification

- Extension: `13 passed` with Node's test runner.
- Extension manifest JSON and JavaScript syntax validated.
- Backend: `10 passed in 3.40s` with pytest, including API, configuration,
  OpenRouter retry/validation, and Supabase storage coverage.
- Android: clean `assembleDebug lintDebug compileDebugUnitTestKotlin`
  completed successfully.
- Android JVM tests: `7 passed` across authentication validation, calendar,
  dashboard summaries, and reminder planning.
- Android lint completed with `0 errors` and `33 warnings`; the warnings are
  dependency-version and style suggestions.
- The current debug APK is
  `frontend/app/build/outputs/apk/debug/app-debug.apk` (`22,823,756` bytes),
  built at `2026-06-11 16:42:10 +08:00`.
- Emulator QA covered English onboarding and login, language persistence,
  encrypted cold-start profile restoration, all dashboard counters, weekly
  progress, extension disconnected/connected heartbeat states, four-event
  Smart Extraction preview/save, calendar selection, event details, confirmed
  deletion, and reminder scheduling.
- The deterministic runtime extractor exercised the real Android-to-FastAPI
  request path without requiring an OpenRouter key.
- Android scheduled future Miyad `ReminderReceiver` alarms and logcat contained
  no `AndroidRuntime` crash during the completed flow.
- In-app Browser startup was attempted, but Windows denied its helper process
  before a page opened, so extension browser automation remained unavailable.

### Environment and assumptions

- No new secrets or required environment variables were introduced.
- Production now requires the already documented Supabase, OpenRouter, JWT, and
  explicit CORS variables to be non-placeholder values.
- Extension connection is considered recent for seven days after an
  authenticated heartbeat.

### Remaining external verification

- Apply the Supabase schema to a real project and exercise the service-role RPC.
- Run live OpenRouter extraction with a supplied API key.
- Test against a signed-in Outlook Web App session.
- Validate local alarms on representative physical Android devices; FCM push
  delivery remains outside the implemented local-reminder scope.
