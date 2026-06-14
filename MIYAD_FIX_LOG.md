# Miyad Fix Log

For each code modification applied to repair and transition the Miyad academic scheduling assistant project to production readiness.

---

## 1. JWT Session Expiration Claim
* **File Path:** [backend/app/core/security.py](file:///c:/مشاريع/Miyad1/backend/app/core/security.py)
* **What Changed:** Introduced `exp` (expiration) claim in `create_access_token` defaulting to 7 days, and enforced validation of `exp` and `type` during decoding.
* **Why:** The previous audit identified JWT tokens without an expiration date, allowing permanent API access to compromised tokens.
* **Risk Level:** Low (standard security practice, minor impact on client token persistence duration).
* **Test Run After Change:** `pytest` inside the `backend` directory (tests pass successfully, including `test_token_expiration`).

## 2. In-Memory Zero-Dependency Rate Limiter
* **File Path:** [backend/app/core/rate_limit.py](file:///c:/مشاريع/Miyad1/backend/app/core/rate_limit.py)
* **What Changed:** Created an in-memory sliding-window rate limiter using FastAPI Dependencies, checking IP address and path, and evicting expired entries.
* **Why:** The API endpoints were exposed to brute-force auth spam and costly AI extraction spam.
* **Risk Level:** Low (in-memory state resets on server restart, but is highly lightweight and stable).
* **Test Run After Change:** `pytest` inside the `backend` directory (including `test_rate_limiting`).

## 3. Rate Limit Annotations on API Endpoints
* **File Paths:**
  * [backend/app/api/auth.py](file:///c:/مشاريع/Miyad1/backend/backend/app/api/auth.py)
  * [backend/app/api/email.py](file:///c:/مشاريع/Miyad1/backend/backend/app/api/email.py)
* **What Changed:** Added rate limits:
  * `/api/auth/register`: 5 attempts/hour
  * `/api/auth/login`: 5 attempts/minute
  * `/api/process-email`: 30 attempts/minute
  * `/api/extract-manual`: 10 attempts/minute
* **Why:** Protect auth registration/login and OpenRouter-triggering email extraction routes.
* **Risk Level:** Low (student-friendly limit boundaries prevent abuse without interrupting legitimate users).
* **Test Run After Change:** `verify_api.py` and `pytest` pass.

## 4. Rate Limit Restoring in Backend Tests
* **File Path:** [backend/tests/test_api.py](file:///c:/مشاريع/Miyad1/backend/tests/test_api.py)
* **What Changed:** Imported `reset_rate_limiter` and invoked it inside `make_client()` to purge the sliding-window state between unit tests.
* **Why:** Running tests sequentially with the same mock client IP polluted the rate limiting store, leading to false failures.
* **Risk Level:** None (test-only change).
* **Test Run After Change:** `pytest` passes completely (13 tests pass).

## 5. Supabase Row-Level Security Policies
* **File Paths:**
  * [backend/supabase/production_policies.sql](file:///c:/مشاريع/Miyad1/backend/supabase/production_policies.sql) (new)
  * [backend/supabase/schema.sql](file:///c:/مشاريع/Miyad1/backend/supabase/schema.sql) (appended)
* **What Changed:** Defined explicit SELECT, INSERT, UPDATE, and DELETE policies matching `auth.uid() = user_id` for tables `users`, `events`, `email_hashes`, `notification_tokens`, and `user_settings`.
* **Why:** The previous schema activated RLS on all tables but left them without explicit policies, which would result in complete denial for authenticated clients or direct leakage if service role credentials were misconfigured.
* **Risk Level:** Medium (improper RLS configuration could cause database access issues, but verified to be theoretically correct).
* **Test Run After Change:** Schema parses cleanly. RLS simulation written in `rls_test.sql`.

## 6. Supabase Transactional RLS Testing Script
* **File Path:** [backend/supabase/rls_test.sql](file:///c:/مشاريع/Miyad1/backend/supabase/rls_test.sql) (new)
* **What Changed:** Created a complete SQL verification script simulating two users (`student_a@example.com` and `student_b@example.com`) and verifying that policies successfully block cross-user read/write/delete operations.
* **Why:** To enable database administrators to audit and verify policies before deploying.
* **Risk Level:** None (runs inside transactional rollback).
* **Test Run After Change:** Verified SQL syntax and structure.

## 7. Android Network Security Hardening
* **File Path:** [frontend/app/src/main/res/xml/network_security_config.xml](file:///c:/مشاريع/Miyad1/frontend/app/src/main/res/xml/network_security_config.xml) (new)
* **What Changed:** Created a network security configuration permitting cleartext HTTP traffic strictly to local loopback addresses (`10.0.2.2`, `localhost`) and enforcing HTTPS globally for all other domains.
* **Why:** Disables cleartext traffic in release builds to protect API payloads over untrusted networks.
* **Risk Level:** Low (safeguards mobile API calls).
* **Test Run After Change:** Clean Gradle build and unit tests pass.

## 8. Android Manifest Manifest Network References
* **File Path:** [frontend/app/src/main/AndroidManifest.xml](file:///c:/مشاريع/Miyad1/frontend/app/src/main/AndroidManifest.xml)
* **What Changed:** Removed the global `android:usesCleartextTraffic="true"` property and linked `android:networkSecurityConfig="@xml/network_security_config"`.
* **Why:** Restricts HTTP cleartext traffic strictly to development domains instead of globally.
* **Risk Level:** Low.
* **Test Run After Change:** Android compile passes.

## 9. Android Build Variants Configuration
* **File Path:** [frontend/app/build.gradle.kts](file:///c:/مشاريع/Miyad1/frontend/app/build.gradle.kts)
* **What Changed:** Defined `debug`, `staging`, and `release` build variants:
  * `debug`: API pointing to local loopback `http://10.0.2.2:8000/`.
  * `staging`: API pointing to `https://staging-api.miyad.app/`.
  * `release`: API pointing to `https://api.miyad.app/`, enabled Proguard shrinking (`isMinifyEnabled = true`), and linked conditional signing configurations from environment variables if present.
* **Why:** Avoid hardcoded backend URLs and enforce code compilation safety in release builds.
* **Risk Level:** Low.
* **Test Run After Change:** Gradle compile passes.
