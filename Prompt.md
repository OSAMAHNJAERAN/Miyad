You are an expert full-stack developer. I already have an existing app and browser extension project. Do not rebuild from scratch. Your task is to inspect the current codebase, understand the existing structure, fix the current issues, improve the UI/UX, connect the extension with the main app/backend properly, and fully test everything until it works smoothly.

Project context:
The app is an Arabic/English student appointment and scheduling app called “Meead / ميعاد”. It includes a mobile-style UI, calendar, events, schedule pages, statistics, account page, and a browser extension. The current design uses a dark/green modern theme with a bright lime accent color. Keep the same general identity, but polish and improve the implementation.

Main required fixes and improvements:

1. Fix the extension connection issue
- The extension currently shows a “Failed to fetch” error during login/register or backend connection.
- Debug the extension API requests and make sure the extension can communicate correctly with the backend.
- Check CORS, base API URL, environment variables, request headers, authentication flow, and API response handling.
- Make sure the extension and the main app are connected to the same backend.
- Test the extension in the browser manually after fixing it.
- Confirm that login, signup, remote connection, fetching data, and saving data all work correctly.
- Do not leave placeholder endpoints or dummy fetch logic unless clearly marked as development-only.

2. Backend requirement: FastAPI + PostgreSQL
Use FastAPI for the backend because it is fast, async-based, scalable, and can handle high request loads.

Use PostgreSQL as the main database, preferably through Supabase free tier or a standard PostgreSQL connection.

Required backend stack:
- FastAPI
- PostgreSQL
- SQLAlchemy or SQLModel
- Alembic migrations if suitable
- Pydantic schemas
- Async database operations where appropriate
- Secure authentication flow
- Environment variables through `.env`
- Proper CORS setup for both the web app and browser extension

The backend must support:
- User signup
- User login
- Authentication/session/token handling
- User profile data
- Events/appointments CRUD
- Calendar event fetching
- Language preference
- University field
- Extension-to-backend communication

3. Fix account/signup/login screen
Based on the first reference screenshot:
- Keep the dark theme and lime green accent.
- Fix the “Failed to fetch” error.
- Ensure all form fields are correctly validated:
  - Name
  - Email
  - Password
  - University
- Make Arabic layout RTL clean and consistent.
- Add clear error messages in Arabic and English depending on selected language.
- Improve the login/signup toggle so it feels polished and responsive.
- Ensure the “Remote connect / الاتصال بميعاد” button actually connects to the backend.

4. Event and appointment system
Improve the appointment/event system so it works like Google Calendar in terms of flexibility.

Each event must be independent and customizable. For every event, the user should be able to set:
- Event title
- Description
- Date
- Start time
- End time
- All-day option
- Repeating option:
  - Does not repeat
  - Daily
  - Weekly
  - Monthly
  - Custom repeat if possible
- Location or online link if suitable
- Reminder/notification option if suitable
- Event color/category if suitable

Important:
- Do not make all appointments follow one global time.
- Each event must have its own date, time, duration, and repeat settings.
- Calendar, schedule page, home page, and extension must all read from the same event data source.
- Events should be saved in PostgreSQL and fetched through FastAPI.

5. Calendar UI improvement
In the calendar page:
- If a single day has multiple events, show multiple small dots under the day number.
- Each dot should represent one event.
- If there are many events, show up to a reasonable limit such as 3 dots, then indicate more events if needed.
- Tapping/clicking the day should show the list of events for that date.
- Support both Arabic RTL and English LTR layouts.

6. Bottom navigation bar redesign
Use the second reference image as the visual inspiration for the bottom navigation bar.

Requirements:
- Bottom bar should have smooth rounded corners.
- It should look modern, soft, floating, and polished.
- Keep the current concept but improve the design.
- Change the icons and labels to fit this app.
- Replace the current middle scan button with a large centered plus button.
- The plus button should be lime green and visually elevated.
- When the user taps the plus button, it should open the “Add Event” screen/modal.
- Suggested navigation items:
  - Home / الرئيسية
  - Calendar or Schedule / جدولي
  - Add Event button in the center / +
  - Statistics / إحصائياتي
  - Account / حسابي
- Use proper Arabic and English labels based on selected language.
- Make active/inactive states clear.
- Keep the design responsive for mobile.

7. Replace the current image/logo asset
The current image shown in the third reference screenshot must be replaced inside the app code and assets.

Replace it with the new provided image asset named:
“ChatGPT Image Jun 10, 2026, 08_08_15 PM”

Important:
- Replace it everywhere it appears: splash screen, logo area, loading screen, empty states, or onboarding if used.
- Update asset imports and paths correctly.
- Do not leave the old image referenced in the code.
- Make sure the new image appears cleanly and is not stretched or pixelated.

8. Language selection improvement
Do not make language switching happen only by pressing a simple toggle button.

Instead, implement a clear language selector:
- Arabic
- English

The language selector can be:
- Dropdown
- Segmented control
- Settings option
- First-time onboarding selector

Requirements:
- Arabic should use RTL layout.
- English should use LTR layout.
- All main texts, labels, buttons, errors, and navigation items should be translated.
- Store the selected language in user preferences or local storage/database.
- The selected language should persist after refresh/restart.

9. Integration between app, extension, and backend
Make sure the whole system works together:
- Main app uses FastAPI backend.
- Browser extension uses the same backend.
- PostgreSQL stores users and events.
- Login/signup works from both app and extension if applicable.
- Events created in the app should appear in the extension if the extension displays events.
- Events created from the extension should appear in the app if extension event creation exists.
- No disconnected mock data should remain in production flow.

10. Testing requirement
Do not stop after coding. You must test the project.

Testing checklist:
- Run backend server.
- Check database connection.
- Run frontend/app.
- Load browser extension.
- Test signup.
- Test login.
- Test Arabic language.
- Test English language.
- Test add event.
- Test all-day event.
- Test daily repeating event.
- Test event with custom start/end time.
- Test multiple events on the same calendar day.
- Test calendar dots under the date.
- Test bottom navigation plus button.
- Test extension API connection.
- Confirm “Failed to fetch” is fixed.
- Confirm there are no console errors.
- Confirm there are no broken imports or missing assets.

11. Code quality
- Keep the codebase clean and organized.
- Do not hardcode API keys or secrets.
- Use `.env.example` for required environment variables.
- Use clear file names.
- Remove unused code and unused assets if safe.
- Add comments only where helpful.
- Keep UI components reusable.
- Keep backend routes organized by feature.
- Make the project easy to run locally.

12. Documentation
Create or update a file named `change.md`.

Every change you make must be documented inside `change.md`.

The file must include:
- Files created
- Files modified
- Backend changes
- Frontend/UI changes
- Extension changes
- Database changes
- Environment variables added
- Assets replaced
- Bugs fixed
- Testing performed
- Any assumptions made
- Remaining notes if something requires manual setup such as Supabase credentials

13. Final expected result
The final result must be a working polished system:
- FastAPI backend connected to PostgreSQL/Supabase
- Main app connected to backend
- Browser extension connected to backend
- No “Failed to fetch” error
- Professional Arabic/English UI
- Google Calendar-like event creation
- Multiple dots under calendar dates with multiple events
- Modern rounded bottom navigation bar with centered plus button
- New image asset replacing the old one
- Fully tested and documented in `change.md`

Important:
Do not only explain or plan. Implement the fixes directly in the codebase, run the project, test it, and document everything.