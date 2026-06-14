# Design QA

## References and captures

- Floating navigation reference:
  `C:\Users\ac-98\OneDrive\Pictures\Screenshots\Screenshot 2026-06-10 194716.png`
- Logo reference:
  `C:\Users\ac-98\OneDrive\Pictures\Screenshots\Screenshot 2026-06-13 084404.png`
- Android light authentication:
  `screenshots/runtime_glass_current.png`
- Android dark authentication:
  `screenshots/runtime_glass_auth_dark.png`
- Earlier connected Home and Add Event references remain in
  `screenshots/runtime_latest.png` and `screenshots/runtime_add_event.png`.

## Android review

- System theme switching was verified on the emulator in light and dark modes.
- RTL hierarchy, labels, password visibility, touch sizing, and keyboard-safe
  scrolling render without clipping on the medium-phone emulator.
- Dark surfaces use tonal green glass with readable off-white text.
- Light surfaces use warm off-white backgrounds and translucent white cards.
- No Miyad `AndroidRuntime` crash appeared during install, launch, theme switch,
  or authentication interaction.
- Native picker and event scheduling output are covered by JVM tests.

## Extension review

- Real Miyad logo replaces the CSS-drawn mark.
- Popup bindings use complete Arabic/English dictionaries with key-parity tests.
- Static tests verify local icon references, system/light/dark selectors,
  backdrop-blur fallback, and reduced-motion CSS.
- Theme and language persistence plus authenticated `/api/settings`
  synchronization are covered by service-worker tests.
- Existing health, auth, recent-event, retry, backend URL, and heartbeat tests
  continue to pass.

## Verification results

- Backend: `11 passed`.
- Extension: `22 passed`.
- Android: JVM unit tests passed.
- Android lint: passed.
- Android debug APK assembly: passed.
- APK: `frontend/app/build/outputs/apk/debug/app-debug.apk`.

## QA limitation

The in-app browser bridge was unavailable because the Windows sandbox could not
start its browser helper. The extension was therefore verified through automated
DOM/CSS/i18n tests rather than captured browser screenshots. A final automated
authenticated Android screen capture was also interrupted by the tool usage
ceiling; light and dark authentication captures and runtime crash checks were
completed successfully.

Final result: implementation and automated verification passed, with the two
capture limitations documented above.
