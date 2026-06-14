You are a senior full-stack repair agent, QA automation engineer, security engineer, Supabase engineer, Chrome Extension MV3 tester, Android release engineer, and student-focused product reliability engineer.

Repository:
https://github.com/OSAMAHNJAERAN/Miyad.git

Project name:
Miyad / ميعاد

Your mission:
Take the existing Miyad project from “ready for demo only” to the strongest possible staging/production-ready state by fixing the known issues, testing the real system, verifying Supabase, testing Outlook Web App extraction, testing the Chrome Extension in a real browser, testing the Android app on emulator/device, and producing a complete repair + evidence report.

Important:
Do not rebuild the project from scratch.
Do not change the product idea.
Do not remove existing features.
Do not replace FastAPI, Supabase, OpenRouter, Chrome Extension MV3, Kotlin, or Jetpack Compose unless absolutely necessary.
Do not expose secrets.
Do not commit real API keys.
Use only authorized staging/test accounts and test credentials provided by the project owner.
Use browser automation, real browser clicks, emulator, terminal, Supabase, and Outlook Web App if credentials are available.
If any credential or environment access is missing, mark the item as BLOCKED and explain exactly what is needed. Do not fake a pass.

Known current status from previous audit:

* Backend pytest passed.
* Backend local integration passed.
* Chrome Extension unit tests passed.
* Android debug APK builds and launches.
* Browser E2E for Chrome Extension was blocked by environment.
* Android JVM tests failed because the Windows project path contained Arabic characters.
* Final verdict was READY FOR DEMO ONLY.
* Main remaining risks:

  1. JWT tokens do not expire.
  2. Supabase production RLS policies must be verified and hardened.
  3. Outlook Web App DOM selectors must be tested on a real signed-in Outlook session.
  4. OpenRouter real AI extraction must be tested with a real key.
  5. Production URLs and CORS must be configured.
  6. Android release signing and HTTPS-only production config must be prepared.
  7. Rate limiting is missing.

Required credentials and inputs:
Use environment variables only. Never hardcode secrets.

Expected variables:

* SUPABASE_URL
* SUPABASE_ANON_KEY
* SUPABASE_SERVICE_KEY
* SUPABASE_DB_PASSWORD or DATABASE_URL if direct SQL is needed
* OPENROUTER_API_KEY
* OPENROUTER_MODEL
* JWT_SECRET
* PRODUCTION_API_URL
* STAGING_API_URL
* OUTLOOK_TEST_EMAIL
* OUTLOOK_TEST_PASSWORD
* OUTLOOK_TEST_DOMAIN
* ANDROID_KEYSTORE_PATH, only if release signing is being tested
* ANDROID_KEYSTORE_ALIAS
* ANDROID_KEYSTORE_PASSWORD
* ANDROID_KEY_PASSWORD

If Outlook login requires MFA or manual authentication:
Open the browser and allow manual login if supported.
If manual login is not possible, create a mock OWA page test, but mark real Outlook validation as BLOCKED.

Main repair goals:

1. Fix JWT session security in a student-friendly way
   Cause:
   The previous audit found persistent JWT tokens without expiration. This is convenient but risky if a token is stolen.

Required solution:
Implement a reasonable student-friendly session model:

* Access token with expiration.
* Refresh token or long-lived session renewal.
* Keep the user experience smooth so students are not forced to log in constantly.
* Recommended:

  * Access token: 1 day or 7 days depending on current app needs.
  * Refresh token: 30 days.
  * Logout revokes or clears refresh token.
  * Token refresh endpoint.
  * Android app refreshes token automatically.
  * Extension refreshes token automatically or requests login again gracefully.
* Store refresh tokens securely.
* Do not log tokens.
* Add tests for:

  * expired access token rejected
  * valid refresh token returns new access token
  * logout invalidates token
  * invalid refresh token rejected
  * extension handles expired token gracefully
  * Android handles expired token gracefully

If full refresh-token infrastructure is too large for the current project, implement a safer minimum:

* Add `exp` claim to JWT.
* Add clear re-login behavior.
* Document the limitation and future refresh-token plan.

2. Add rate limiting
   Cause:
   The backend can be abused by repeated login or AI extraction requests.

Required solution:
Add rate limiting to FastAPI using a simple maintainable solution such as slowapi or reverse-proxy-compatible middleware.

Protect at least:

* /api/auth/login
* /api/auth/register
* /api/process-email
* /api/manual-extract
* OpenRouter-triggering endpoints

Use reasonable limits for students:

* Login: 5 attempts per minute per IP/email.
* Register: 5 attempts per hour per IP.
* Process email: 30 requests per minute per authenticated user.
* Manual extraction: 10 requests per minute per authenticated user.

Required tests:

* normal request passes
* repeated spam requests return 429
* error message is user-friendly
* rate limit does not break normal student flow

3. Harden Supabase production schema and RLS
   Cause:
   Previous audit said schema exists and RLS is enabled, but production policies must be explicitly verified.

Required solution:
Inspect backend/supabase/schema.sql.
Create or update a production-safe Supabase migration file.

Required tables to protect:

* users
* events
* email_hashes
* user_settings

Required policies:

* A user can only SELECT their own rows.
* A user can only INSERT rows with their own user_id.
* A user can only UPDATE their own rows.
* A user can only DELETE their own rows.
* email_hashes must be scoped by user_id.
* events must be scoped by user_id.
* user_settings must be scoped by user_id.
* RPC function save_extraction must be secured.
* Service role access must remain backend-only.

Required verification:
Use two real Supabase test users:

* [student_a@example.com](mailto:student_a@example.com)
* [student_b@example.com](mailto:student_b@example.com)

Test:

* User A creates event.
* User B cannot read User A event.
* User B cannot update User A event.
* User B cannot delete User A event.
* User A cannot insert event with User B user_id.
* Duplicate email hash is checked per user, not globally.
* User A and User B can process the same academic email independently without cross-account conflict.
* Direct Supabase client access with anon key respects RLS.
* Service key is never exposed to Android or Extension.

Deliver:

* backend/supabase/schema.sql updated if needed.
* backend/supabase/production_policies.sql if useful.
* backend/supabase/rls_test.sql or script-based policy test.
* Supabase evidence screenshots or SQL output logs if possible.

4. Configure production/staging environment safely
   Cause:
   Current project is mostly local/demo configured.

Required solution:
Create clear environment separation:

* development
* staging
* production

Backend:

* ENVIRONMENT=production must reject unsafe values.
* DATABASE_BACKEND=supabase required in production.
* JWT_SECRET required and strong.
* CORS_ORIGINS must not be `*` in production.
* SUPABASE_URL and SUPABASE_SERVICE_KEY required in production.
* OPENROUTER_API_KEY required if AI extraction is enabled.

Extension:

* Do not hardcode localhost for production.
* Allow safe configurable backend URL.
* Validate backend URL:

  * must be https in production
  * must reject javascript:, file:, data:, and embedded credentials
  * must reject suspicious URLs
* Manifest host permissions should be minimal.

Android:

* Debug can use http://10.0.2.2:8000.
* Release must use HTTPS production API.
* cleartext traffic must be disabled in release.
* BuildConfig should separate debug/staging/release API URLs.

5. Test real OpenRouter AI extraction
   Cause:
   Previous test used FakeExtractor when no API key was available.

Required solution:
Use real OPENROUTER_API_KEY on staging only.
Test the actual model with realistic academic emails.

Create test dataset:

* English assignment deadline
* Arabic assignment deadline
* mixed Arabic/English email
* exam schedule
* quiz reminder
* class cancellation
* class rescheduling
* event with date only
* event with date + time
* multiple deadlines in one email
* email with no academic event
* malformed text
* very long email
* ambiguous date
* timezone-sensitive event

Verify:

* JSON is valid.
* Date is correct.
* Time is correct.
* Timezone is correct.
* Event type is correct.
* No event is created for irrelevant email.
* Multiple events are extracted correctly.
* AI malformed output is handled safely.
* Raw email body is not stored permanently.
* Cost/rate-limit behavior is reasonable.

If model quality is weak:

* Improve prompt in backend/app/services/llm.py.
* Add stricter schema validation.
* Add retry with corrective prompt.
* Add safe fallback to manual confirmation before saving.

6. Test real Outlook Web App with Chrome Extension
   Cause:
   Previous browser E2E was blocked. Real Outlook DOM must be verified.

Required solution:
Use a real browser.
Load the extension unpacked:

* Open chrome://extensions
* Enable Developer Mode
* Load unpacked extension folder
* Open extension popup
* Set backend URL to staging or local backend
* Register or login with test student account
* Open Outlook Web App using authorized test student account
* Open test academic emails
* Confirm extraction works

Required real Outlook tests:

* Open a normal academic email.
* Confirm subject, sender, timestamp, and body are detected.
* Confirm extension sends request to backend.
* Confirm backend stores event.
* Confirm duplicate email is skipped.
* Confirm extension retry queue works when backend is offline.
* Turn backend off, process email, confirm queued state.
* Turn backend on, confirm queue flushes successfully.
* Test Arabic UI.
* Test English UI.
* Test RTL/LTR switching.
* Test dark/light/system mode.
* Test manual extraction from selected text if available.
* Test popup error messages.
* Test logout.
* Test expired token behavior after JWT fix.
* Test only authorized Outlook pages are matched.
* Confirm extension does not request Outlook password.
* Confirm extension does not store raw email body permanently.

If Outlook DOM selectors fail:

* Inspect current DOM.
* Update content script selectors.
* Add fallback selectors.
* Avoid observing document.body directly.
* Keep MutationObserver scoped to reading pane or stable parent only.
* Add tests for new selectors.
* Add a user-friendly message: “No readable email selected.”

7. Test Chrome Extension security
   Check:

* Manifest V3 compliance.
* No unsafe eval.
* No remote scripts.
* Minimal permissions.
* CSP safe.
* No API keys in extension.
* No service key in extension.
* Backend URL validation.
* XSS-safe rendering in popup.
* No raw HTML injection from email.
* Local storage only contains safe preference/token data.
* Tokens are not logged.
* Dedup hash is stable.
* Retry queue is bounded.

Fix any issue found and add tests.

8. Test Android app deeply
   Cause:
   APK builds, but production and real integration must be verified.

Required solution:
Move project to ASCII-only path first if running on Windows:
Example:
C:\Miyad1

Then run:

* gradlew clean
* gradlew testDebugUnitTest
* gradlew lintDebug
* gradlew assembleDebug
* gradlew assembleRelease if signing config is available

Emulator/device tests:

* Install APK.
* Launch app.
* Register test user.
* Login.
* Token saved securely.
* Logout clears token.
* Reopen app and verify session behavior.
* Test expired token behavior after JWT fix.
* Test dashboard.
* Test calendar.
* Test add event.
* Test edit event.
* Test delete event.
* Test all-day event.
* Test recurring event.
* Test invalid start/end validation.
* Test Arabic.
* Test English.
* Test RTL/LTR.
* Test dark/light/system theme.
* Test offline backend.
* Test malformed backend response.
* Test extension connection status.
* Test event created by Outlook extension appears in Android calendar.
* Test calendar dots show correct event date.
* Test statistics screen uses real backend data.
* Test accessibility: readable text, no clipping, buttons clickable, keyboard works.

Security checks:

* TokenStore uses encrypted storage.
* No tokens in logcat.
* No passwords in logcat.
* Release disables cleartext HTTP.
* Release uses HTTPS API.
* Production build does not use debug server URL.
* Proguard/R8 enabled if suitable.
* Release signing prepared.

9. Full real end-to-end scenario
   Run the complete realistic flow:

Step 1:
Deploy backend locally or staging with Supabase enabled.

Step 2:
Apply Supabase schema and RLS policies.

Step 3:
Create test student account.

Step 4:
Load Chrome Extension in real Chrome.

Step 5:
Login to extension.

Step 6:
Open Outlook Web App.

Step 7:
Open this test email:
Subject:
CS101 Assignment Deadline

Body:
Dear students, your CS101 assignment is due on 25 June 2026 at 11:59 PM Malaysia time. Please submit it through the LMS before the deadline.

Expected:

* Extension reads the email.
* Extension sends email metadata/body to backend safely.
* Backend uses real OpenRouter if available.
* Backend extracts event.
* Event is stored in Supabase.
* Duplicate email does not create duplicate event.
* Android app shows the event.
* Calendar displays dot on 25 June 2026.
* Event details are correct.
* Raw email body is not stored permanently.

Run at least these additional E2E cases:

* Arabic academic email.
* English exam email.
* Email with no academic deadline.
* Email with two deadlines.
* Backend offline then online retry.
* Duplicate message.
* User A and User B isolation.

10. Student-focused reasonable solutions
    When fixing issues, keep the app suitable for students:

* Login should not be annoying.
* Error messages should be simple.
* Offline behavior should be friendly.
* Do not create expensive AI calls unnecessarily.
* Do not store unnecessary personal email data.
* Keep privacy clear.
* Use Arabic and English messages.
* Keep interface fast and smooth.
* Prefer simple maintainable fixes over enterprise-heavy complexity.

Examples:

* For JWT: use refresh token, not constant re-login.
* For AI failure: show “Could not extract event. Try manual add.”
* For Outlook scrape failure: show “Open one email first.”
* For backend offline: queue safely and retry later.
* For duplicate email: show “Already processed.”
* For rate limit: show “Too many attempts. Please try again later.”

11. Required files to create or update
    Create these files at repository root:

12. MIYAD_REPAIR_AND_REAL_E2E_REPORT.md
    Must include:

* Executive summary
* Original issues
* Cause of each issue
* Fix applied
* Files changed
* Test evidence
* Screenshots path
* What passed
* What failed
* What was blocked
* Security result
* Production readiness status
* Final verdict

2. MIYAD_FIX_LOG.md
   For every code change:

* File path
* What changed
* Why
* Risk level
* Test run after change

3. MIYAD_REAL_TEST_RESULTS.json
   Machine-readable JSON:

* backend_tests
* supabase_tests
* extension_tests
* outlook_real_tests
* android_tests
* e2e_tests
* security_tests
* production_checklist
* blocked_items

4. MIYAD_STUDENT_DEMO_SCRIPT.md
   Simple student demo script:

* How to run backend
* How to load extension
* How to login
* How to open Outlook
* How to send/process email
* How to see event in Android
* What to say during presentation

5. qa-evidence/
   Store:

* screenshots
* terminal outputs
* browser console logs
* Android logcat snippets
* Supabase policy test outputs
* extension popup screenshots
* Outlook test screenshots if privacy allows

Redact:

* passwords
* tokens
* keys
* personal student data
* real email content unless test-only

12. Required final report format
    For each issue, use this structure:

Issue ID:
Severity:
Area:
Cause:
Impact:
Fix Applied:
Files Changed:
Test Performed:
Result:
Evidence:
Remaining Risk:
Next Step:

Issues that must be covered:

* JWT without expiration
* Missing rate limiting
* Supabase RLS production policies
* Supabase schema deployment
* OpenRouter real model validation
* Outlook live DOM validation
* Chrome Extension live browser E2E
* Android JVM test path issue
* Android production API URL
* Android release signing
* Android HTTPS-only release config
* Extension production API URL
* Extension host permissions
* CORS production restrictions
* Secrets management
* Raw email privacy
* Duplicate email processing
* Multi-user isolation
* Token logs
* Extension retry queue
* AI malformed JSON handling
* Arabic/English RTL/LTR behavior

13. Commands to run
    Backend:
    cd backend
    python -m venv .venv
    .venv\Scripts\python -m pip install -r requirements.txt
    .venv\Scripts\pytest
    .venv\Scripts\python verify_api.py
    .venv\Scripts\uvicorn app.main:app --reload --host 127.0.0.1 --port 8000

Extension:
cd extension
npm install
npm test

Android:
Move repo to ASCII path first if needed.
cd frontend
gradlew clean
gradlew testDebugUnitTest
gradlew lintDebug
gradlew assembleDebug

If signing config is available:
gradlew assembleRelease

Browser:
Use Chrome or Chromium.
Load extension unpacked.
Use DevTools console.
Use screenshots.
Use Playwright/Selenium only if available and permitted.

Supabase:
Apply schema to staging.
Verify RLS policies.
Run two-user isolation tests.
Record SQL or script results.

14. Do not mark as passed unless verified
    Rules:

* If you did not run it, do not say PASS.
* If you simulated it, mark SIMULATED, not PASS.
* If credentials are missing, mark BLOCKED.
* If environment prevents browser/emulator, mark BLOCKED with exact reason.
* If you fixed code but did not retest, mark FIXED_NOT_VERIFIED.
* If a fix is too large, create a safe minimal fix and document the full future fix.

15. Final expected verdict
    At the end, choose one:

* READY FOR PRODUCTION
* READY FOR STAGING
* READY FOR DEMO ONLY
* NOT READY

Only say READY FOR PRODUCTION if:

* Supabase production/staging RLS is verified.
* Real Outlook extension test passed.
* Real OpenRouter extraction passed.
* Android release config is safe.
* Production URLs are configured.
* CORS is restricted.
* No secrets are exposed.
* Critical/high security issues are fixed.
* Full E2E from Outlook to backend to Supabase to Android passed.

Start now:

1. Read all docs and previous reports.
2. Identify known issues.
3. Fix issues one by one.
4. Run tests after each fix.
5. Use browser, Supabase, Outlook, and Android emulator where possible.
6. Write final reports with evidence.
