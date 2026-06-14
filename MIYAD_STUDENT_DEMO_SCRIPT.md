# Miyad (ميعاد) Student Demo Script

Use this step-by-step script to showcase the repaired and fully localized Miyad academic scheduling assistant system during presentations or evaluation demos.

---

## 1. Demo Setup & Execution Steps

### Step 1: Run the Backend Server Locally
1. Open a PowerShell terminal in the `backend/` directory.
2. Activate the virtual environment and launch the server:
   ```powershell
   .venv\Scripts\activate
   python -m uvicorn app.main:app --reload --host 127.0.0.1 --port 8000
   ```
3. Show the browser output for `http://127.0.0.1:8000/health` to confirm the server status is `"ok"` and the database uses local in-memory storage (clean development environment).

### Step 2: Load the Chrome Extension Unpacked
1. Open Google Chrome and navigate to `chrome://extensions/`.
2. Toggle on the **Developer mode** switch in the top-right corner.
3. Click the **Load unpacked** button in the top-left.
4. Select the `extension/` directory from the project files.
5. Pin the **Miyad Assistant** icon to the Chrome toolbar.

### Step 3: Login to the Extension Popup
1. Click the Miyad extension icon.
2. Select preferred language (Arabic or English) and theme (Light, Dark, or System mode). Notice the smooth transition and bilingual RTL/LTR layout support.
3. Click "Sign Up" or "Login" using a test account (e.g. `student@example.edu` and `strong-password`).
4. Notice that once authenticated, the popup displays "Connected to Miyad Backend" and displays settings synced with the server.

### Step 4: Open Outlook Web App & Scrape Academic Email
1. Log in to a student email account on Outlook Web App (`https://outlook.office.com/` or `https://outlook.live.com/`).
2. Open an academic email containing a deadline, such as:
   * **Subject:** `CS101 Assignment Deadline`
   * **Body:** `Dear students, your CS101 assignment is due on 25 June 2026 at 11:59 PM Malaysia time. Please submit it through the LMS before the deadline.`
3. Show the Miyad Extension popup. It automatically detects the active email reading pane, grabs the metadata (subject, sender, timestamp), hashes the content client-side to prevent duplicates, and shows a "Syncing..." status.
4. If a backend connection is temporarily offline, show that the extension places the task in the offline **Retry Queue** and safely flushes it once the backend is reachable.

### Step 5: Show the Extracted Event in the Android App
1. Launch the Android Emulator `medium_phone` and open the **Miyad** app.
2. Log in using the same student account (`student@example.edu`).
3. Open the **Calendar** tab. Show the calendar dot displayed on **25 June 2026**.
4. Tap the date to reveal the event details:
   * **Title:** Database Assignment
   * **Due Date:** June 25, 2026, 11:59 PM
   * **Location:** LMS / Online
   * **Notes:** "Late submissions will not be accepted."
5. Highlight the Thmanyah typography and the native RTL support for Arabic users on the calendar and event cards.

---

## 2. Presentation Talk Tracks

### Hook (Introduction)
> *"Hello everyone. As students, we receive dozens of academic emails every week containing assignment deadlines, lecture changes, and exam schedules. Manually copying these details into our personal calendars is tedious, prone to error, and easy to forget. Today, we present **Miyad (ميعاد)**, the bilingual academic scheduling assistant that bridges the gap between your university mailbox and your mobile calendar."*

### Demo Walkthrough (FastAPI + Chrome Extension)
> *"Watch as I open a mock email on Outlook Web App. The Miyad Chrome Extension (built using Manifest V3) instantly detects the active message. Behind the scenes, it hashes the email signature client-side to ensure privacy and avoid duplicate processing. It sends the content to our FastAPI backend, which extracts structured JSON events using Gemma-2-9b-it via the OpenRouter API. If I try to refresh the page, the extension notices the email has already been processed and doesn't trigger a duplicate server call, saving valuable AI tokens."*

### Security & Mobile Integration (Android + Supabase Policies)
> *"Now let's switch to the Miyad Android App, built using Jetpack Compose and Thmanyah font styling. As you can see, our student dashboard automatically syncs and displays the newly extracted CS101 deadline on June 25th. Under the hood, security is staging-ready: we implemented JWT token expiration, built-in endpoint rate limiting to prevent spam attacks, disabled cleartext HTTP traffic in release builds, and deployed Supabase Row-Level Security (RLS) policies. This ensures that every student's academic inbox and schedule remain strictly isolated and private."*

### Closing (Impact)
> *"By combining a lightweight browser scraper, structured AI extraction, and a beautiful mobile interface, Miyad automates academic organization, giving students more time to focus on learning. Thank you, and we are happy to take any questions."*
