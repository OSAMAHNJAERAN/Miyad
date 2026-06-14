# Miyad Unified Glass Design System

## Product character

Miyad is a calm, premium academic productivity product shared by Android and
the Chrome extension. Both surfaces use layered tonal backgrounds, restrained
translucency, crisp type, lime actions, and semantic event colors.

Android uses performant tonal transparency and gradients. The extension uses
`backdrop-filter` with an opaque token fallback.

## Color tokens

| Token | Light | Dark | Use |
|---|---|---|---|
| Primary | `#B8F23A` | `#B8F23A` | Actions, selection, success |
| Background | `#F5F7F2` | `#081008` | Root canvas |
| Text | `#101610` | `#F5F7F2` | Primary content |
| Glass | `rgba(255,255,255,.72)` | `rgba(19,32,20,.78)` | Standard surfaces |
| Glass strong | `rgba(255,255,255,.90)` | `rgba(23,39,25,.94)` | Forms and navigation |
| Border | translucent white | translucent green | Surface definition |
| Muted | `#667066` | `#B4C0B2` | Metadata |
| Danger | `#E95454` | `#FF9A9A` | Errors and delete actions |

## Typography

- Thmanyah Serif Display: splash, onboarding heroes, and major page headings.
- Thmanyah Sans: body copy, forms, controls, cards, navigation, and metadata.
- Android and extension use bundled local font assets with system fallbacks.
- Text remains readable under font scaling; controls do not rely on fixed text
  heights.

## Geometry

- Spacing scale: `4, 8, 12, 16, 20, 24, 32, 40`.
- Glass cards: `24dp/px` radius.
- Form controls and primary buttons: `18dp/px` radius.
- Floating navigation: `40dp` radius with a centered lime Add action.
- Minimum touch target: `44dp/px`.
- Borders are one pixel/dp and shadows stay low-opacity.

## Shared components

- Glass background and standard/strong glass cards.
- Section headings and status pills.
- Lime primary actions and quiet outlined secondary actions.
- Inline field errors, loading buttons, skeletons, retry banners, empty states,
  confirmation dialogs, and success feedback.
- Status always combines color with copy or an icon.

## Android behavior

- Theme mode is `system`, `light`, or `dark` and is stored in encrypted local
  preferences.
- Direction changes immediately from the account language and mirrors
  directional navigation.
- Bottom navigation hides while the IME is visible.
- Add Event uses native date/time pickers, essential fields first, expandable
  optional details, and a fixed save action.
- Statistics uses a Canvas donut based on real event counts.
- Continuous backdrop blur is intentionally avoided for API 24+ performance.

## Extension behavior

- Popup width is `390px`, with responsive internal layout and no horizontal
  overflow.
- Theme follows `prefers-color-scheme` in system mode and persists manual
  overrides.
- Browser locale selects the pre-login language; authenticated language is
  synchronized through `GET/PATCH /api/settings`.
- All visible copy is bound through complete Arabic and English dictionaries.
- Icons and the Miyad logo are local assets; no remote font or icon dependency
  is required.
- Status and form updates use `aria-live`, visible focus rings, disabled/loading
  controls, and inline validation.

## Motion and accessibility

- Screen fades: `160-220ms`; selection feedback uses short spring/tween motion.
- Motion never blocks input or changes layout dimensions.
- Extension animations are disabled by `prefers-reduced-motion`.
- Primary text and actions target WCAG AA contrast in both themes.
