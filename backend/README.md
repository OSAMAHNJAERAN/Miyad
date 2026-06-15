# Miyad Backend

FastAPI service for authentication, AI event extraction, Supabase persistence,
event management, and bilingual user preferences.

## Run locally

```powershell
cd backend
py -m venv .venv
.\.venv\Scripts\python -m pip install -r requirements.txt
Copy-Item .env.example .env
.\.venv\Scripts\python -m uvicorn app.main:app --reload
```

The default `DATABASE_BACKEND=memory` is useful for local UI development.
Production should use `DATABASE_BACKEND=supabase` after applying
`supabase/schema.sql`.

The migration enables row-level security on every Miyad table. Its
`save_extraction` function atomically writes the email hash and extracted
events and is executable only by the backend `service_role`.

## Test

```powershell
.\.venv\Scripts\python -m pytest
```

OpenAPI documentation is available at `http://127.0.0.1:8000/docs`.

The extension uses a two-step trust flow:

- `POST /api/preview-email` calls OpenRouter and returns structured events
  without writing an email hash or event.
- `POST /api/confirm-extraction` stores the reviewed result atomically and
  prevents duplicate processing by `(user_id, email_hash)`.
- `POST /api/confirm-manual-extraction` stores the Android user's edited review
  without calling AI a second time or dropping confidence/source evidence.

Extracted events include confidence, source subject/sender, evidence, course,
assignment, lecturer, location, notes, and timezone-aware date/time fields.

`POST /api/events` creates independent manual events with timezone-aware start
and end times, all-day state, recurrence, reminder, color, description, and
location. AI-extracted events retain `due_date` compatibility and are mapped to
the same scheduling fields.

## Security

- AI and Supabase service keys only exist in backend environment variables.
- JWT access tokens intentionally have no expiry and are removed on explicit logout.
- Raw email bodies are processed in memory and are never persisted.
- Email fingerprints are unique per user through `(user_id, hash)`.
- Production startup fails if Supabase, OpenRouter, JWT, or explicit CORS
  configuration is missing.
- `POST /api/extension/heartbeat` records authenticated extension activity
  without storing Outlook credentials or message bodies.
