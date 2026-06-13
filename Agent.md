# 🤖 agent.md — Miyad Project Blueprint
> **Version:** 2.0.0 | **Last Updated:** 2026-06-10
> **Purpose:** Single source of truth for all AI agents working on this codebase. Every agent (Claude, Gemini, Cursor, Copilot, etc.) must read this file and `design.md` before writing a single line of code.

---

## 📌 Project Identity

| Field | Value |
|---|---|
| **Product Name** | Miyad (ميعاد) |
| **Tagline** | Your academic deadlines, captured automatically |
| **Core Problem** | University Outlook emails contain critical deadlines. The university blocks API access, third-party apps, and auto-forwarding. |
| **Core Solution** | A browser extension that scrapes email content client-side (no server permissions needed), sends it to Gemini 2.5 Flash for extraction, and syncs structured events to the student's personal calendar. |
| **Primary Users** | University students using Outlook Web App (OWA) |

---

## 🧭 Agent Rules (Read Before Anything Else)

1. **Always read `design.md` alongside `Agent.md` before writing code.** You must adhere to the design system (Thmanyah fonts, Limon Green/Black/White scheme, custom components) defined in `design.md`.
2. **Never break the no-server-permission constraint.** All email reading happens in the browser, on the client side only. No university server credentials are ever touched.
3. **Manifest V3 is non-negotiable.** Chrome has deprecated MV2. All extension code must be MV3-compliant.
4. **Gemini 2.5 Flash is the designated AI model.** Do not substitute with other models unless explicitly instructed.
5. **All AI calls go through the backend — never directly from the extension.** The extension sends raw text to our own backend, and the backend calls Gemini. This protects the API key.
6. **Duplicate prevention is a core feature, not an afterthought.** Every email must be hashed before processing.
7. **Write modular, clean, commented code.** Other agents will continue your work.
8. **Never store raw email body in the database.** Store only the extracted structured JSON after AI processing.
9. **JWT tokens never expire automatically.** Sessions are persistent and end ONLY when the user explicitly clicks "Log Out". Do NOT set `expiresIn` when signing tokens. Do NOT write token expiry checks in middleware.

---

## 🏗️ System Architecture Overview

```
┌──────────────────────────────────────────────────────────────────┐
│                        STUDENT BROWSER                           │
│                                                                  │
│  ┌─────────────────────────────────────────────┐                │
│  │        Outlook Web App (OWA)                │                │
│  │                                             │                │
│  │  [Email Opens] ──► MutationObserver         │                │
│  │                         │                   │                │
│  │                    content.js               │                │
│  │                    (scrapes text)           │                │
│  └──────────────────────┬──────────────────────┘                │
│                         │                                        │
│                    background.js                                 │
│                    (Service Worker)                              │
│                         │                                        │
│                    [Hashing + Dedup]                             │
│                         │                                        │
└─────────────────────────┼────────────────────────────────────────┘
                          │ HTTPS POST
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                      OUR BACKEND (Node.js)                      │
│                                                                  │
│   /api/process-email                                            │
│        │                                                        │
│        ├──► Validate token (JWT)                                │
│        ├──► Check duplicate hash (Supabase)                     │
│        ├──► Call Gemini 2.5 Flash API                           │
│        │         └── Returns: structured JSON (events)          │
│        └──► Save events to Supabase                             │
│             └──► Trigger push notification to mobile app        │
└──────────────────────────────┬──────────────────────────────────┘
                               │
              ┌────────────────┴─────────────────┐
              ▼                                   ▼
    ┌──────────────────┐               ┌──────────────────────┐
    │   Supabase DB    │               │  Mobile App (Expo)   │
    │   (PostgreSQL)   │               │  React Native        │
    │                  │               │                      │
    │  - users         │               │  - Calendar view     │
    │  - events        │               │  - Deadline alerts   │
    │  - email_hashes  │               │  - Push notifs       │
    └──────────────────┘               └──────────────────────┘
```

---

## 📁 Full Repository Structure

```
miyad/
│
├── Agent.md                            # ← THIS FILE (blueprint)
├── design.md                           # Brand and UI design system (Thmanyah fonts, colors, components)
│
├── extension/                          # Chrome/Edge Extension (MV3)
│   ├── manifest.json
│   ├── src/
│   │   ├── content.js                  # OWA scraper
│   │   ├── background.js               # Service Worker
│   │   ├── popup/
│   │   │   ├── popup.html
│   │   │   ├── popup.js
│   │   │   └── popup.css
│   │   └── utils/
│   │       ├── hasher.js               # SHA-256 email hash
│   │       └── logger.js               # Dev logging utility
│   ├── fonts/                          # Custom Thmanyah WOFF2 font files
│   └── icons/
│       ├── icon-16.png
│       ├── icon-48.png
│       └── icon-128.png
│
├── backend/                            # Node.js + Express API
│   ├── src/
│   │   ├── server.js                   # Entry point
│   │   ├── routes/
│   │   │   ├── email.routes.js         # POST /api/process-email
│   │   │   ├── auth.routes.js          # POST /api/auth/login, register
│   │   │   └── events.routes.js        # GET  /api/events
│   │   ├── controllers/
│   │   │   ├── email.controller.js
│   │   │   └── events.controller.js
│   │   ├── services/
│   │   │   ├── gemini.service.js       # Gemini 2.5 Flash integration
│   │   │   ├── supabase.service.js     # DB operations
│   │   │   └── notification.service.js # Push notifications
│   │   ├── middleware/
│   │   │   ├── auth.middleware.js      # JWT validation
│   │   │   └── rateLimit.middleware.js
│   │   └── utils/
│   │       ├── hash.js
│   │       └── prompt.js               # Gemini prompt builder
│   ├── .env.example
│   └── package.json
│
└── Mobile App/                         # Android App (Kotlin + Jetpack Compose)
    ├── build.gradle.kts
    └── app/
        ├── build.gradle.kts
        └── src/main/
            ├── AndroidManifest.xml
            ├── java/com/example/
            │   ├── MainActivity.kt     # Launch activity & Splash/UI Reference
            │   ├── data/               # Data layer (Room DB, Retrofit, Repository)
            │   │   ├── AppDatabase.kt
            │   │   ├── api/
            │   │   ├── dao/
            │   │   ├── model/
            │   │   └── repository/
            │   └── ui/                 # View components & Compose screens
            │       ├── MiyadApp.kt     # App Shell, Navigation & main Compose layouts
            │       ├── theme/          # Custom theme configuration
            │       │   ├── Color.kt    # Miyad brand colors
            │       │   ├── Theme.kt    # Light/Dark scheme mapping
            │       │   └── Type.kt     # Thmanyah typography setup
            │       └── viewmodel/      # MiyadViewModel for screen states
            └── res/
                ├── font/               # Custom Thmanyah OTF font files
                └── drawable/
```

---

## 🛠️ Technology Stack

### 1. Chrome Extension

| Layer | Technology | Reason |
|---|---|---|
| **Manifest** | Manifest V3 | Chrome standard, required for store submission |
| **Content Script** | Vanilla JavaScript (ES2020+) | No build step needed, runs directly in page context |
| **Service Worker** | Vanilla JavaScript | MV3 replaces background pages with service workers |
| **Popup UI** | HTML + CSS + Vanilla JS | Lightweight, no framework overhead |
| **Hashing** | Web Crypto API (`crypto.subtle`) | Built-in, no dependencies, SHA-256 |
| **Storage** | `chrome.storage.sync` | Syncs user token across devices |

> ✅ **No npm, no build tools required for the extension.** Keep it zero-dependency.

---

### 2. Backend (API Server)

| Layer | Technology | Reason |
|---|---|---|
| **Runtime** | Node.js 20 LTS | Stable, async-native, wide ecosystem |
| **Framework** | Express.js 5 | Minimal, well-understood, easy to extend |
| **AI Integration** | `@google/generative-ai` SDK | Official Gemini SDK |
| **Database Client** | `@supabase/supabase-js` | Real-time + simple CRUD |
| **Auth** | JWT (`jsonwebtoken`) | Stateless, mobile-friendly |
| **Rate Limiting** | `express-rate-limit` | Prevent API abuse |
| **Validation** | `zod` | TypeScript-friendly schema validation |
| **Environment** | `dotenv` | Secrets management |
| **Deployment** | Railway or Render | Free tier, auto-deploy from GitHub |

---

### 3. Database (Supabase / PostgreSQL)

#### Schema

```sql
-- Users table
CREATE TABLE users (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email       TEXT UNIQUE NOT NULL,
  name        TEXT,
  university  TEXT,
  created_at  TIMESTAMPTZ DEFAULT NOW()
);

-- Events extracted from emails
CREATE TABLE events (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id       UUID REFERENCES users(id) ON DELETE CASCADE,
  title         TEXT NOT NULL,
  course_code   TEXT,
  event_type    TEXT CHECK (event_type IN ('exam', 'deadline', 'lecture', 'other')),
  due_date      TIMESTAMPTZ NOT NULL,
  location      TEXT,
  notes         TEXT,
  source_hash   TEXT REFERENCES email_hashes(hash),
  created_at    TIMESTAMPTZ DEFAULT NOW()
);

-- Email deduplication
CREATE TABLE email_hashes (
  hash        TEXT PRIMARY KEY,
  user_id     UUID REFERENCES users(id) ON DELETE CASCADE,
  subject     TEXT,
  processed_at TIMESTAMPTZ DEFAULT NOW()
);
```

---

### 4. Mobile App

| Layer | Technology | Reason |
|---|---|---|
| **Language** | Kotlin | Standard language for modern Native Android development |
| **Framework** | Jetpack Compose | Modern declarative UI toolkit for Android |
| **Local Database** | Room Database | SQLite abstraction layer, officially recommended |
| **Networking** | Retrofit 2 + OkHttp | Industry standard HTTP client for REST APIs |
| **Concurrency** | Kotlin Coroutines & Flow | Asynchronous programming & reactive state streams |
| **State Management** | Architecture Components ViewModel | Preserves state across configuration changes |
| **Auth & Sync** | JWT Session & Supabase API | Seamless user session & event syncing |

---

## 🔄 Detailed Data Flow

### Step 1 — Extension Scraping (content.js)
```
User navigates to OWA in browser
        │
        ▼
URL Polling (setInterval, 500ms) watches for conversation ID change
in the address bar (e.g. owa/#path/mail/id/AAMk...)
        │
        ▼  (new conversation ID detected)
Locate the Reading Pane container
  → Try: '.ReadingPaneContents'  (Classic OWA)
  → Try: '[aria-label="Message body"]' (New OWA)
  → STOP if not found — do NOT fall back to document.body
        │
        ▼
Attach MutationObserver ONLY to the Reading Pane element
{ subtree: true, childList: true, characterData: true }
        │
        ▼
Wait for email body selector to stabilize (debounce 300ms)
        │
        ▼
Extract:
  - subject:   .SubjectHeader or [aria-label="Subject"]
  - sender:    .ms-Persona-primaryText (full email address preferred)
  - timestamp: <time datetime="..."> ISO attribute value
  - body:      full innerText of email body container
  - timezone:  Intl.DateTimeFormat().resolvedOptions().timeZone
               (e.g. "Asia/Kuala_Lumpur" — injected automatically)
        │
        ▼
Generate SHA-256 hash = hash(subject + sender_email + timestamp + body.slice(0, 100))
  → 4-factor fingerprint guarantees uniqueness even for follow-up emails
        │
        ▼
Check chrome.storage.local for this hash
  ├── EXISTS → skip (already processed)
  └── NEW    → send to background.js
```

### Step 2 — Background Worker (background.js)
```
Receive message from content.js
        │
        ▼
Retrieve user_token from chrome.storage.sync
        │
        ▼
POST https://api.miyad.app/api/process-email
Body: {
  user_token: "...",
  metadata: { sender, subject, timestamp, timezone },
  raw_content: "...",
  email_hash: "sha256:..."
}
        │
        ├── Success → store hash locally, update popup badge
        └── Failure → queue for retry (exponential backoff)
```

### Step 3 — Backend Processing
```
Receive POST /api/process-email
        │
        ▼
Validate JWT token → get user_id
        │
        ▼
Check email_hashes table for duplicate
  ├── DUPLICATE → return 200 { status: "already_processed" }
  └── NEW       → continue
        │
        ▼
Build Gemini prompt (see Prompt Design section)
        │
        ▼
Call Gemini 2.5 Flash API
        │
        ▼
Parse JSON response → validate with Zod schema
        │
        ▼
Insert into events table
        │
        ▼
Insert hash into email_hashes table
        │
        ▼
Trigger push notification to student's device
        │
        ▼
Return 201 { status: "success", events_created: N }
```

---

## 🧠 Gemini Prompt Design

### System Prompt (set once in backend)
```
You are an academic schedule extraction assistant.
Your ONLY job is to read university email content and extract academic events.
You MUST return ONLY valid JSON. No explanation. No markdown. No preamble.
If no academic events are found, return: {"events": []}
All datetime values in your output MUST be converted to strict ISO 8601 format
using the student's local timezone provided in the prompt. Never assume UTC.
```

### User Prompt Template
```
Extract all academic deadlines, exams, quizzes, assignments, and events 
from the following university email.

Email Subject: {{subject}}
Email Sender: {{sender}}
Email Date: {{timestamp}}
Student Local Timezone: {{timezone}}   ← IMPORTANT: Use this timezone when
                                          converting ALL times to ISO 8601.
                                          e.g. "Tuesday at 2 PM" must be
                                          resolved using this timezone.

Email Body:
---
{{raw_content}}
---

Return a JSON object with this exact structure:
{
  "events": [
    {
      "title": "string (descriptive event name)",
      "course_code": "string or null (e.g. CS101)",
      "event_type": "exam | deadline | quiz | lecture | other",
      "due_date": "ISO 8601 datetime string (timezone-aware, not UTC-assumed)",
      "location": "string or null (room, building, or 'Online')",
      "notes": "string or null (any extra relevant info)"
    }
  ]
}
```

### Zod Validation Schema (backend)
```typescript
const EventSchema = z.object({
  title: z.string().min(1),
  course_code: z.string().nullable(),
  event_type: z.enum(['exam', 'deadline', 'quiz', 'lecture', 'other']),
  due_date: z.string().datetime(),
  location: z.string().nullable(),
  notes: z.string().nullable(),
});

const GeminiResponseSchema = z.object({
  events: z.array(EventSchema),
});
```

---

## 🔐 Security Model

| Concern | Solution |
|---|---|
| **API Key Exposure** | Gemini API key lives ONLY in backend `.env`. Never in extension. |
| **User Auth** | JWT issued at login, stored in `chrome.storage.sync` (encrypted by Chrome). No expiry — session ends only on explicit logout. |
| **Data in Transit** | All requests over HTTPS only |
| **Email Content** | Raw body sent to backend, processed, then DISCARDED. Only structured JSON is stored. |
| **Duplicate Prevention** | SHA-256 hash of `subject + sender_email + timestamp + body.slice(0,100)` — 4-factor fingerprint prevents collisions on follow-up emails |
| **Rate Limiting** | 20 requests/hour per user token on the backend |
| **University Compliance** | Extension reads only what the user already sees. No credentials. No API access. Fully client-side. |

---

## 🌐 OWA Selectors (Outlook Web App)

These CSS selectors are used in `content.js`. They must be tested against both **Classic OWA** and **New OWA**:

```javascript
const SELECTORS = {
  // Email reading pane
  emailBody: [
    '[aria-label="Message body"]',          // New OWA
    '.ReadingPaneContents',                  // Classic OWA
    '[data-testid="message-body"]',          // Alternate
    '.allowTextSelection'                    // Fallback
  ],
  subject: [
    '.SubjectHeader',
    '[data-testid="ConversationReadingPaneSubject"]',
    'h1[aria-level="2"]',
    '[role="heading"].allowTextSelection',   // Live OWA specific
    'div.JdFsz'                              // Live OWA fallback
  ],
  sender: [
    '[aria-label*="From"]',                  // Robust Live OWA attribute
    '.ms-Persona-primaryText',
    '[data-testid="senderName"]',
    'span[title*="@"]'
  ],
  timestamp: [
    'time[datetime]',
    '[data-testid="sent-date"]',
    '.metadata-timestamp',
    'div.srQCs'                              // Live OWA specific class
  ]
};
```

> ⚠️ **Agent Note:** Microsoft frequently updates OWA DOM structure. Use multiple fallback selectors and log when none match.
>
> **Performance Rule:** The `MutationObserver` must ONLY be attached to the resolved Reading Pane element — NEVER to `document.body`. Observing the full document body causes severe browser lag on OWA's heavy dynamic DOM.
>
> **Triggering Strategy:** Use a lightweight `setInterval` URL poll (500ms) to detect conversation ID changes in `window.location.href`. Only when a new conversation ID is detected should the extension attempt to locate the Reading Pane and (re)attach the observer. This avoids continuous DOM observation and is the only MV3-compliant approach that doesn't degrade performance.

---

## 📋 API Endpoints Reference

### Authentication
```
POST /api/auth/register
Body: { email, password, name, university }
Returns: { user, token }

POST /api/auth/login
Body: { email, password }
Returns: { user, token }
```

### Email Processing
```
POST /api/process-email
Headers: Authorization: Bearer <token>
Body: {
  metadata: {
    sender:    string,       // full sender email address
    subject:   string,
    timestamp: string,       // ISO 8601
    timezone:  string        // e.g. "Asia/Kuala_Lumpur"
  },
  raw_content: string,
  email_hash:  string        // sha256:<hash>
}
Returns: {
  status: "success" | "already_processed" | "no_events_found",
  events_created: number
}
```

### Events
```
GET /api/events
Headers: Authorization: Bearer <token>
Query: ?from=ISO_DATE&to=ISO_DATE&type=exam|deadline|all
Returns: { events: Event[] }

DELETE /api/events/:id
Headers: Authorization: Bearer <token>
Returns: { success: true }
```

---

## 📦 Environment Variables

### Backend `.env`
```env
# Server
PORT=3000
NODE_ENV=production

# Supabase
SUPABASE_URL=https://xxxx.supabase.co
SUPABASE_SERVICE_KEY=your-service-role-key

# Gemini AI
GEMINI_API_KEY=your-gemini-api-key
GEMINI_MODEL=gemini-2.5-flash

# JWT
JWT_SECRET=a-very-long-random-secret-string
# ⚠️ NO JWT_EXPIRES_IN — tokens are persistent by design.
# Sessions terminate only via explicit user logout (token deletion from storage).

# Push Notifications (Expo)
EXPO_ACCESS_TOKEN=your-expo-token
```

### Extension Storage Keys
```javascript
// chrome.storage.sync
STORAGE_KEYS = {
  USER_TOKEN:  'miyad_user_token',
  USER_EMAIL:  'miyad_user_email',
  SYNC_STATUS: 'miyad_sync_status',
}

// chrome.storage.local (session, dedup cache)
LOCAL_KEYS = {
  PROCESSED_HASHES: 'miyad_processed_hashes', // Array of recent hashes
}
```

---

## 🚀 Development Setup

### Extension (No Build Required)
```bash
1. Open Chrome → chrome://extensions
2. Enable "Developer Mode" (top right toggle)
3. Click "Load unpacked" → select the /extension folder
4. Navigate to your university Outlook Web App
5. Open any email → check DevTools console for logs
```

### Backend
```bash
cd backend
cp .env.example .env        # Fill in your keys
npm install
npm run dev                  # Starts on http://localhost:3000
```

### Mobile App
```bash
cd "Mobile App"
./gradlew assembleDebug
# Open the "Mobile App" folder with Android Studio, sync Gradle, and run on a connected Android device or emulator.
```

---

## 🗺️ Development Phases & Priorities

### Phase 1 — Core Extension MVP (Week 1-2)
- [ ] `manifest.json` with correct permissions and match patterns
- [ ] `content.js` with MutationObserver and multi-selector email scraping
- [ ] `background.js` service worker with POST to backend
- [ ] SHA-256 deduplication logic
- [ ] Basic `popup.html` showing connection status

### Phase 2 — Backend + AI (Week 2-3)
- [ ] Express server with `/api/process-email` endpoint
- [ ] JWT authentication middleware
- [ ] Supabase schema setup and client integration
- [ ] Gemini 2.5 Flash service with prompt engineering
- [ ] Zod validation of AI output
- [ ] Duplicate hash checking in DB

### Phase 3 — Mobile App (Week 3-5)
- [x] Kotlin + Jetpack Compose app architecture and UI
- [ ] Local persistence with Room Database
- [ ] Auth screens (login/register) connected to backend API
- [ ] Calendar view showing extracted events
- [ ] Upcoming deadlines list sorted by urgency
- [ ] Sync manager for background data refreshing
- [ ] Push notifications for upcoming deadlines

### Phase 4 — Polish & Launch (Week 5-6)
- [ ] Error handling and retry logic in extension
- [ ] Multi-university OWA URL support
- [ ] Complete Arabic language support (RTL) in mobile app UI and charts
- [ ] Chrome Web Store submission
- [ ] Beta testing with real students

---

## ⚠️ Known Constraints & Decisions

| Constraint | Decision | Reason |
|---|---|---|
| OWA DOM changes frequently | Use multiple fallback selectors | Resilience over simplicity |
| MV3 service workers can sleep | Use `chrome.alarms` for retry queue | MV3 limitation workaround |
| Gemini may hallucinate dates | Validate all dates with Zod `.datetime()` after extraction | Data integrity |
| Students may have multiple email accounts | Scope extension to university email domains only via `manifest.json` match patterns | Privacy & focus |
| Raw email may be in Arabic or English | Gemini 2.5 Flash is multilingual — no extra config needed | Gemini capability |
| Observing full `document.body` causes lag | MutationObserver targets Reading Pane only; URL polling detects email changes | Performance |
| Follow-up emails share same subject/timestamp | Hash includes `sender_email + body.slice(0,100)` for unique 4-factor fingerprint | Deduplication accuracy |
| Gemini doesn't know student's local time | `timezone` (from `Intl.DateTimeFormat`) injected into every payload and prompt | ISO 8601 date correctness |
| Persistent login required | JWT signed without `expiresIn`; logout deletes token from `chrome.storage.sync` | UX requirement |

---

## 🔗 Key Resources

- [Chrome Extension MV3 Docs](https://developer.chrome.com/docs/extensions/mv3/)
- [Gemini API Reference](https://ai.google.dev/gemini-api/docs)
- [Supabase Docs](https://supabase.com/docs)
- [Jetpack Compose Docs](https://developer.android.com/compose)
- [OWA DOM Inspector Guide](https://learn.microsoft.com/en-us/outlook/troubleshoot/)

---

> **For AI Agents:** When you receive a task referencing this project, read this file and `design.md` first. Follow the architecture exactly. When in doubt, ask before inventing. Keep all code modular so other agents can continue your work cleanly.
>
> **Last Modified By:** Antigravity via Gemini | June 2026 | v2.0.0