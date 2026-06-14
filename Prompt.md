You are a senior full-stack engineer, UI/UX motion designer, and system architect.

You are working on an existing production-level project called “Meead / ميعاد”, a student scheduling + calendar + browser extension system.

IMPORTANT:
- DO NOT rebuild the project from scratch.
- Work directly inside the existing repository.
- Analyze first, then modify.
- Ensure everything is fully functional and tested.

GitHub Repository (SOURCE OF TRUTH):
https://github.com/OSAMAHNJAERAN/Miyad.git

---

## 🔥 CORE GOAL
Transform this project into a highly polished, modern, premium-quality application with:
- Smooth animations
- Beautiful micro-interactions
- Seamless UI transitions
- Fully working backend + extension integration
- Calendar system like Google Calendar
- Professional production-level architecture

---

# 1. FULL SYSTEM FIX (CRITICAL)
Fix all current broken functionality:

### Extension Issue
- Fix "Failed to fetch" error in browser extension
- Ensure proper API connection between extension and backend
- Fix CORS issues
- Fix base URL misconfiguration
- Ensure authentication works from extension

---

# 2. BACKEND ARCHITECTURE (MANDATORY)

Use industry-standard backend stack:

### MUST USE:
- FastAPI (async backend)
- PostgreSQL (via Supabase or direct DB)
- SQLAlchemy / SQLModel
- Pydantic schemas
- JWT authentication
- dotenv environment management

### Backend must support:
- User authentication (signup/login)
- Event CRUD system
- Calendar event retrieval
- Language preference storage
- Extension + App shared data access

---

# 3. EVENT SYSTEM (GOOGLE CALENDAR STYLE)

Each event must be independent.

Event fields:
- Title
- Description
- Date
- Start time
- End time
- All-day toggle
- Repeat options:
  - None
  - Daily
  - Weekly
  - Monthly
  - Custom (if possible)
- Location / online link
- Reminder option
- Event color/category

IMPORTANT:
- Each event has its own time system
- Do NOT use global scheduling
- All data stored in PostgreSQL
- All platforms read same data source

---

# 4. CALENDAR UI IMPROVEMENTS

- If multiple events exist on a day:
  - Show multiple dots under the date number
  - Max 3 visible dots, then "+more" indicator if needed
- Clicking a date shows full event list
- Smooth transitions when opening day details

---

# 5. BOTTOM NAVIGATION BAR REDESIGN

Use modern floating pill-style navigation:

### Requirements:
- Fully rounded corners
- Floating glass / soft UI style
- Smooth shadows
- Center action button replaces scan icon

### Center button:
- Large "+"
- Lime green
- Elevated (floating effect)
- On click → opens “Add Event” modal/screen

### Navigation items:
- Home / الرئيسية
- Calendar / جدولي
- + Add Event (center)
- Statistics / إحصائياتي
- Account / حسابي

---

# 6. LANGUAGE SYSTEM

Replace toggle with proper language selector:

- Arabic (RTL layout)
- English (LTR layout)

Requirements:
- Persistent language storage
- Full UI translation:
  - buttons
  - labels
  - errors
  - navigation
- Instant UI switching without reload issues

---

# 7. IMAGE / ASSET REPLACEMENT

Replace existing image asset:

“ChatGPT Image Jun 10, 2026, 08_08_15 PM”

Replace everywhere:
- Splash screen
- Logo
- Loading states
- Empty states
- Onboarding screens

Ensure:
- No stretching
- Proper scaling
- Optimized rendering

---

# 8. UI/UX MOTION DESIGN (VERY IMPORTANT 🔥)

Upgrade entire UI with premium animations.

You MUST implement:

### A. Micro-interactions
- Button press animations (scale down → bounce back)
- Card hover lift effect
- Smooth ripple effect on tap
- Input field focus glow animation

---

### B. Page transitions
- Smooth fade + slide transitions between screens
- Bottom nav switch animation (active icon morph effect)
- Modal open/close with spring animation

---

### C. Calendar animations
- Date selection highlight animation (smooth circle expand)
- Event dots appear with fade + scale
- Month switching slide animation

---

### D. Add Event animation
- Plus button expands into modal
- Form fields animate sequentially (stagger animation)
- Save button success animation (checkmark pulse)

---

### E. Background UI enhancement
- Subtle animated gradient lights in background
- Soft floating glow particles (very low opacity)
- Smooth ambient motion (non-distracting)

---

### F. Icon animations
- Active icon slightly bounces or glows
- Smooth icon transitions (not static switching)

---

IMPORTANT:
- Animations must be smooth (60fps)
- Use modern libraries like Framer Motion (or equivalent)
- Keep performance optimized (no lag)
- Do NOT over-animate (keep premium minimal style)

---

# 9. FULL INTEGRATION REQUIREMENTS

Ensure all systems are connected:

- Web App ↔ FastAPI Backend
- Browser Extension ↔ FastAPI Backend
- PostgreSQL ↔ All data storage
- Events sync across all platforms
- No mock data in production flow

---

# 10. TESTING REQUIREMENTS (MANDATORY)

After implementation:

Test:
- Signup/Login
- Extension connection
- Event creation
- Event repetition
- Calendar multi-event dots
- Language switching
- Arabic RTL layout
- Animation performance
- API stability
- No console errors

---

# 11. CODE QUALITY

- Clean architecture
- Modular components
- Reusable UI components
- No hardcoded secrets
- Proper .env usage
- Remove unused code
- Organized folder structure

---

# 12. DOCUMENTATION

Create/update:
change.md

Must include:
- All fixes
- All UI upgrades
- Backend changes
- Extension fixes
- Database schema
- Animation libraries used
- Issues fixed (especially "Failed to fetch")
- Testing results

---

# FINAL GOAL

Deliver a production-ready system that feels:
- Smooth like a modern mobile app
- Beautiful like a premium SaaS product
- Fully functional across app + extension
- Highly animated with premium UX motion design
- Stable backend with FastAPI + PostgreSQL

Do not stop at coding.
Do not leave incomplete features.
Fully implement, test, and ensure everything works together.