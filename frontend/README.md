# Miyad Android App

Native Kotlin application built with Jetpack Compose and Material 3.

## Implemented flow

- Animated splash
- Four-page onboarding and Arabic/English language selection
- RTL/LTR switching with persistent preference
- Backend-connected login and registration
- Encrypted persistent JWT/profile storage and explicit logout
- Dashboard with today/this-week summaries, weekly progress, all five event
  counters, loading, error, and empty states
- Six-week interactive month calendar with date selection, event indicators,
  adjacent-month navigation, and type filters
- Event details with per-event reminder overrides and confirmed deletion
- Manual AI extraction preview and save flow
- Editable AI review cards with confidence, source evidence, date/time,
  course, location, and notes before saving
- Notification/reminder preferences backed by local Android alarms
- Same-day, one-day, and one-week reminders with reboot/package-update restore
- Backend-derived extension connection status and privacy note
- Client-side empty-field, email, and password validation

## Architecture

- `data/`: Retrofit models/API, OkHttp client, repository, encrypted token store.
- `notifications/`: reminder planning, AlarmManager scheduling, notification
  delivery, and boot restoration.
- `ui/AppViewModel.kt`: application state and coroutine operations.
- `ui/product/ProductApp.kt`: product flow and Compose screens.
- `ui/product/CalendarLogic.kt`: testable month-grid and date filtering logic.
- `theme/`: Miyad color and Thmanyah typography.

The debug base URL is configured in `app/build.gradle.kts` as
`http://10.0.2.2:8000/`, which reaches a host machine from the Android emulator.
Change `API_BASE_URL` for a physical device or deployed backend.

## Build and verify

```powershell
java -classpath .\gradle\wrapper\gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain testDebugUnitTest assembleDebug lintDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Fonts

The included Thmanyah font files are used under their bundled license. The app
uses Serif Display for premium headings, Serif Text for descriptions, and Sans
for controls and metadata.
