# Miyad Design System

## Brand

Miyad is a premium academic assistant: calm, precise, spacious, and modern.
The visual signature combines lime green with deep green-black and warm
off-white surfaces.

## Colors

| Token | Value | Use |
|---|---|---|
| Lime | `#B8F23A` | Primary action, selected date, success |
| Dark | `#101610` | Navigation, hero cards, primary text |
| Off white | `#F8F9F6` | App background |
| Card | `#FFFFFF` | Elevated surfaces |
| Light green | `#EAFBC5` | Informational and empty states |
| Muted | `#8C9188` | Secondary text |
| Danger | `#D94949` | Destructive confirmation only |

## Typography

- Hero and major screen titles: Thmanyah Serif Display, bold or black.
- Long descriptions: Thmanyah Serif Text.
- Buttons, metadata, forms, dates, and extension UI: Thmanyah Sans.
- English uses the same families where glyphs are available and system sans
  fallbacks otherwise.

## Spacing and radius

- Spacing scale: `4, 8, 12, 16, 20, 24, 32, 40`.
- Compact controls: `12-16` radius.
- Cards: `18-24` radius.
- Hero/illustration surfaces: `28-45` radius.
- Minimum touch target: `44`.

## Components

- Primary button: dark surface, white label, 18 radius.
- Highlight button: lime surface, dark label.
- Cards: white or dark, generous padding, subtle tonal separation.
- Status: use a dot plus text; never communicate state by color alone.
- Empty states: light-green circular illustration, title, one clear action.
- Loading: restrained skeleton cards or a small lime progress indicator.
- Errors: plain-language reason and a retry action.

## Direction and language

- Arabic uses RTL layout, right-aligned text, and Arabic locale date labels.
- English uses LTR layout and English locale dates.
- Direction changes immediately after language selection and is persisted.
- Directional icons use auto-mirroring where applicable.

## Icon and illustration style

- Rounded geometric line/solid icons.
- Academic motifs: calendar, email, check, book, bell, and graduation cap.
- Lime/black/off-white only, except semantic event colors.
- No copyrighted Outlook artwork; the extension uses a neutral email symbol.

## Motion

- Splash: 650-700 ms fade and scale.
- Screen transition: 160-220 ms crossfade.
- Onboarding: horizontal pager motion.
- Selection and button feedback use native Material interactions.
- Motion must not block input or alter layout dimensions unexpectedly.
