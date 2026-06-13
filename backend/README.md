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

## Security

- AI and Supabase service keys only exist in backend environment variables.
- JWT access tokens intentionally have no expiry and are removed on explicit logout.
- Raw email bodies are processed in memory and are never persisted.
- Email fingerprints are unique per user through `(user_id, hash)`.
- Production startup fails if Supabase, OpenRouter, JWT, or explicit CORS
  configuration is missing.
- `POST /api/extension/heartbeat` records authenticated extension activity
  without storing Outlook credentials or message bodies.
