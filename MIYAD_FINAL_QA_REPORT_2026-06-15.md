# Miyad Final QA Report

Date: 2026-06-15

## Verdict

The repository is materially improved and passes all local automated suites.
Android authentication, event loading, dark mode, system theme, responsive
layout, and the add-event sheet were exercised on a real Android emulator.

The project is ready for staging integration, but it is not honest to call the
external AI/Outlook/Supabase path production-verified until valid service
credentials and a real Outlook session are supplied.

## Broken Before This Pass

- The extension silently processed every detected Outlook message instead of
  showing detection, extraction progress, and a review/confirm step.
- The backend used a repeated fake extractor when OpenRouter was absent.
- Extracted data omitted confidence, evidence, course name, assignment name,
  lecturer, and source email fields.
- Android `debug` targeted `192.168.0.7`, which left emulator login hanging.
- The Android AI review could not edit extracted values and re-ran extraction
  when saving.
- Several fixed light surfaces leaked into dark mode.
- The add-event sheet was not explicitly tied to back/navigation dismissal.
- Fixed-height quick action cards hid secondary text at 130% font scale.

## Implemented

- Required palette applied: `#BCDA4B`, `#030301`, and `#F8FDF4`.
- Rebuilt semantic light/dark surfaces and removed major fixed white cards.
- Added explicit add-sheet back dismissal, navigation dismissal, scrolling,
  keyboard-safe layout, and a single expanded sheet state.
- Added editable Android extraction review and saved the reviewed values.
- Added full Android event editing from the details screen, including title,
  type, date/time, all-day, repeat, notes, location/link, reminder, and color.
- Made the Outlook extraction preview editable before confirmation, and send
  the reviewed values rather than the untouched AI response.
- Added first-class all-day extraction for emails that provide a reliable date
  without a clock time; the backend no longer invents a time.
- Added source subject, sender, evidence, confidence, course, assignment,
  lecturer, and location fields across API, storage, Supabase, extension, and
  Android models.
- Added `/api/preview-email` and `/api/confirm-extraction`.
- Removed the runtime fake AI fallback. Missing OpenRouter configuration now
  produces a clear `503` and `/health` exposes `ai_configured`.
- Changed the extension to detect only, then preview, show confidence/evidence,
  and save after explicit confirmation.
- Preserved SHA-256 per-user duplicate prevention and atomic Supabase RPC save.
- Corrected Android emulator API URL to `http://10.0.2.2:8000/`.

## Automated Results

- Backend: 25 passed, 0 failed on the latest completed run. A final narrow
  normalization change for clearing optional edit fields was added afterward;
  its rerun was blocked by the execution-approval usage limit.
- Chrome extension: 23 passed, 0 failed.
- Android JVM: 12 passed, 0 failed before the new full-event-edit UI.
- Android `assembleDebug`: passed before the new full-event-edit UI.
- Android `lintDebug`: passed with no lint errors before the new
  full-event-edit UI; the required current rerun is blocked by the
  execution-approval usage limit.
- `git diff --check`: passed.
- Mock-transport email matrix covers assignment, exam, cancellation, meeting,
  no-event, Arabic, English, mixed language, vague date, and missing time.

## Emulator Tests

- Light authentication screen: passed.
- Dark authentication screen: passed.
- System/default theme: passed.
- Login against local FastAPI backend: passed.
- Home/dashboard fetch and empty state: passed.
- Add button opens the correct sheet: passed.
- Add sheet remains scrollable: passed.
- Android back dismisses the sheet after scrolling: passed.
- No stuck overlay after dismissal: passed.
- Small screen override (`720x1280`): passed.
- Font scale 130%: tested; fixed hidden quick-action subtitles.

## Evidence

- `qa-evidence/android-runtime-2026-06-15.png`
- `qa-evidence/android-dark-auth-2026-06-15.png`
- `qa-evidence/android-home-fixed-2026-06-15.png`
- `qa-evidence/android-add-open-2026-06-15.png`
- `qa-evidence/android-add-dismissed-2026-06-15.png`
- `qa-evidence/android-small-screen-2026-06-15.png`
- `qa-evidence/android-large-font-2026-06-15.png`
- Matching UI Automator XML files are stored beside relevant screenshots.

## External Verification Still Blocked

- Real OpenRouter extraction: `OPENROUTER_API_KEY` is absent.
- Live Supabase persistence/RLS: Supabase URL and service key are absent.
- Real Outlook DOM and extension popup: no authorized Outlook test session was
  supplied, and the in-app browser process could not start in this sandbox.
- Current Android rebuild/emulator pass after full event editing: blocked when
  the automated permission reviewer hit its usage limit.
- Final backend rerun after the optional-field clearing patch: blocked by the
  same execution-approval limit.

These items are not represented as passing. The code paths have automated
coverage, but staging must run the supplied email matrix with real credentials.
