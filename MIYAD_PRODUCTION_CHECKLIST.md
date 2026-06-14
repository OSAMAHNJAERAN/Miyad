# Miyad Production Readiness & Deployment Checklist

Use this checklist to migrate Miyad from the local development/demo stack to production.

---

## 1. Database (Supabase / PostgreSQL)

* [ ] **Schema Deployment:** Execute the PostgreSQL schema defined in `backend/supabase/schema.sql` on the production database.
* [ ] **Row-Level Security (RLS) Policy Setup:**
  * Define strict SELECT policies: `auth.uid() = user_id` on the `events`, `email_hashes`, and `user_settings` tables.
  * Define INSERT/UPDATE policies allowing access only to authenticated users.
  * Verify that RPC function `save_extraction` is secured and executes using `SECURITY DEFINER` constraints restricted to service role execution.
* [ ] **Database Indexes:** Confirm lookup indexes are active:
  * `idx_users_email` (for quick sign-in checks).
  * `idx_email_hashes_user` (for user deduplication history checks).
  * `idx_events_user_date` (for calendar range queries).

---

## 2. FastAPI Backend Setup

* [ ] **Environment Configuration:** Deploy the application setting the following production-specific variables:
  * `ENVIRONMENT=production`
  * `DATABASE_BACKEND=supabase`
  * `JWT_SECRET=your-secure-32-character-random-hex`
  * `CORS_ORIGINS=chrome-extension://<production-extension-id>` (restrict origins; do not use `*`).
* [ ] **Supabase API Connectivity:** Populate database secrets:
  * `SUPABASE_URL`
  * `SUPABASE_SERVICE_KEY`
* [ ] **OpenRouter AI Keys:** Populate the AI service credentials:
  * `OPENROUTER_API_KEY`
  * Configure `OPENROUTER_MODEL=google/gemma-2-9b-it:free` (or a paid tier if rate limits require scaling).
* [ ] **Rate Limiting:** Enable rate-limiting middleware (e.g. `slowapi` or standard proxy rules) to protect endpoint targets from abuse.

---

## 3. Chrome Extension (MV3) Deployment

* [ ] **API Endpoint:** In the extension code, update the API hostname from `http://127.0.0.1:8000` to the production URL (e.g., `https://api.miyad.app`).
* [ ] **Manifest Permissions:**
  * Update `manifest.json` host matches to include only explicit university OWA domains and the production API URL.
  * Verify `optional_host_permissions` are configured to permit custom user origins safely.
* [ ] **Chrome Web Store Submission:**
  * Compile assets, generate local SVG icon outputs in PNG format (`16x16`, `48x48`, `128x128`).
  * Package the `extension/` directory into a zip archive.
  * Upload to the Chrome Developer Dashboard, populate the privacy disclosure (detailing that raw emails are parsed client-side and never persisted in database storage), and submit for review.

---

## 4. Mobile App (Android) Release

* [ ] **Production API URL:** In `frontend/app/build.gradle.kts`, update the `API_BASE_URL` BuildConfig parameter to point to the production HTTPS URL.
* [ ] **HTTPS Traffic Enforcements:** Remove/disable cleartext network configuration (`cleartextTrafficPermitted=true`) in the production network security configuration to ensure all requests are encrypted.
* [ ] **Release Signing:**
  * Generate a secure release Keystore.
  * Update gradle build script to include `signingConfigs` references matching the production keystore alias and passwords.
* [ ] **Build Proguard Obfuscation:** Enable R8/Proguard code shrinking (`isMinifyEnabled = true`) inside `buildTypes.release` block in `frontend/app/build.gradle.kts` to protect intellectual property.
* [ ] **Release Assembly:** Run `./gradlew assembleRelease` to compile and sign the release AAB/APK bundle.
