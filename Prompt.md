Title: Complete Miyad App Improvement, UI Repair, Real AI Extraction, Extension Integration, and Full End-to-End Testing

Repository:
https://github.com/OSAMAHNJAERAN/Miyad.git

You are required to fully improve, repair, test, and polish the Miyad application until it feels like a complete real production-ready academic schedule assistant, not a rough AI-generated prototype.

Miyad is a bilingual academic schedule assistant. It has:
- Android app frontend using Kotlin Jetpack Compose
- FastAPI backend
- Chrome Manifest V3 extension for Outlook Web App
- OpenRouter AI extraction
- Supabase database support
- Email-to-academic-event extraction workflow

Your task is not only to change colors. You must inspect the whole project, run it, test every screen and every feature, find UI/UX problems, functional bugs, broken AI behavior, dark mode issues, extension issues, API problems, and repair them properly.

Main goal:
Make the app look calm, professional, readable, student-friendly, and fully functional. The app should clearly show that the AI and Outlook extension are working live, not just using repeated fake templates.

Use this color palette as the main design direction:
- Primary accent: #bcda4b
- Main dark text/background: #030301
- Soft light background: #f8fdf4

The current colors feel too bright, harsh, and not comfortable for students. Redesign the app using a calm academic style. Avoid overly saturated, flashy, childish, or random colors.

==================================================
1. FULL UI/UX VISUAL IMPROVEMENT
==================================================

Inspect every screen in the Android app and improve the whole visual design.

Fix these issues:
- Some buttons are not visible enough.
- Some buttons look grey, disabled, or unclear even when they are clickable.
- Some backgrounds are too light while text is also light, making text hard to read.
- Some text blends with the card or background.
- Some text is hidden inside frames, boxes, or cards.
- Some cards have unnecessary borders or extra frames that make the UI look messy.
- Some spacing feels inconsistent.
- Some layouts look like an AI-generated template and not a real app.
- Some UI elements overlap or are clipped.
- Some text is too small or not readable.
- Some icons do not clearly show their function.
- Some screens have weak hierarchy, so the user does not know what to focus on.

Improve:
- Typography
- Button visibility
- Contrast
- Padding
- Spacing
- Card design
- Background design
- Empty states
- Loading states
- Error states
- Success states
- Arabic/English readability
- Alignment
- Responsive behavior on different phone sizes

The app must look clean, calm, and modern for students.

Use #f8fdf4 as the main light background.
Use #030301 for main readable text.
Use #bcda4b as a soft academic accent color, not as an aggressive neon color.
Use softer shades/tints where needed, but keep the palette consistent.

Do not use random colors unless necessary for status indicators such as:
- Success
- Warning
- Error
- Connected
- Disconnected

==================================================
2. DARK MODE MUST BE REBUILT PROPERLY
==================================================

The current dark mode looks very bad and unfinished. Redesign and repair dark mode fully.

Dark mode requirements:
- Must not simply invert colors.
- Must not look like a broken AI-generated theme.
- Text must be readable everywhere.
- Buttons must be clear.
- Cards must have enough contrast from background.
- Borders must be subtle.
- Accent color #bcda4b must still look calm and readable.
- Icons must be visible.
- No hidden text.
- No grey-on-grey unreadable buttons.
- No white cards inside dark background unless intentionally styled.
- All screens must support dark mode properly.

Test every screen in:
- Light mode
- Dark mode
- System default mode

==================================================
3. FIX ADD BUTTON / PLUS BUTTON BUG
==================================================

There is a bug with the plus/add button.

Current issue:
When the user presses the plus button and scrolls or moves down, the add panel/modal/bottom sheet seems to get stuck, stick to the wrong position, or remain open incorrectly.

Fix all problems related to:
- Floating Action Button behavior
- Add event modal
- Bottom sheet
- Dialog position
- Overlay behavior
- Scroll interaction
- Back button dismissal
- Outside tap dismissal
- Keyboard opening/closing
- Z-index/layering problems
- Duplicate modals opening
- Modal sticking after navigation

Expected behavior:
- Pressing plus opens the correct add event screen/modal cleanly.
- It should not stick incorrectly when scrolling.
- It should not block the screen permanently.
- It should close properly with back button or cancel.
- It should not overlap text or hide content.
- It should work in both light mode and dark mode.
- It should work on small and large phone screens.

==================================================
4. FIX HIDDEN TEXT, CLIPPING, AND FRAME PROBLEMS
==================================================

Search the whole UI for:
- Text hidden inside cards
- Text clipped by containers
- Text overflowing outside boxes
- Text covered by frames
- Labels cut off
- Arabic text not fitting
- English text not fitting
- Long subject names being cut badly
- Dates or locations hidden inside event cards
- Buttons partially outside the screen

Repair these issues properly by using:
- Correct Compose layout constraints
- Flexible text wrapping
- maxLines only when needed
- Ellipsis only when appropriate
- Better card height behavior
- Proper scroll containers
- Proper padding and margins
- Responsive layouts

The user should never feel that information is hidden or trapped inside a box.

==================================================
5. REAL AI EXTRACTION MUST WORK, NOT FAKE TEMPLATE
==================================================

The AI extraction currently feels fake or repetitive. When the user extracts from email, it seems to produce the same template for different emails. This is wrong.

Repair the AI workflow so that:
- The AI actually reads the real email content from the Chrome extension.
- It sends the actual current Outlook email content to the backend.
- The backend sends the real content to OpenRouter.
- The extracted result is based on that specific email, not a repeated hardcoded template.
- Different emails must produce different extracted events.
- If the email has no valid academic date/event, the app should clearly say that no academic event was found.
- If the extraction is uncertain, show a clear review step instead of pretending it is correct.

AI must extract:
- Event title
- Course/class name if available
- Assignment name if available
- Due date
- Due time
- Exam date/time
- Meeting date/time
- Location or online link
- Lecturer/sender if useful
- Description/notes
- Confidence level
- Source email subject
- Source email sender
- Extracted raw evidence text from email

Do not let the AI invent information.
Do not create fake dates.
Do not create generic events.
Do not use the same template for every email.

==================================================
6. EXTENSION MUST SHOW LIVE STATUS AND EXTRACTION DETAILS
==================================================

Improve the Chrome extension popup and workflow.

The extension must clearly show:
- Whether the extension is connected or disconnected
- Whether the user is logged in
- Whether the backend is reachable
- Whether the Android/Supabase account is connected
- Current Outlook email detected or not
- Email subject detected
- Email sender detected
- Extract button status
- Loading state while extracting
- Success state after extraction
- Error state if extraction fails
- The extracted details before or after sending to the app

Inside the extension, show something like:
“Detected email: [subject]”
“Extracted event: [title]”
“Date: [date]”
“Time: [time]”
“Confidence: [high/medium/low]”
“Sent to Miyad app successfully”

If no email is open:
Show: “Open an Outlook email first, then try again.”

If backend is not running:
Show: “Backend is not reachable.”

If AI key is missing:
Show a clear error message.

If extraction fails:
Show the actual reason in a user-friendly way.

==================================================
7. END-TO-END WORKFLOW MUST BE REAL
==================================================

Test and repair the complete workflow:

1. User opens Outlook Web App.
2. User opens a real academic email.
3. Chrome extension detects the current email.
4. User clicks extract.
5. Extension sends real email content to backend.
6. Backend calls OpenRouter AI.
7. AI extracts structured academic event data.
8. Backend stores event in Supabase or in-memory dev storage.
9. Android app fetches the event.
10. Event appears correctly in the student’s schedule.
11. User can view, edit, delete, or manage that event.

This must be tested with multiple different email examples:
- Assignment deadline email
- Exam schedule email
- Class cancellation email
- Meeting invitation email
- Email with no academic event
- Email with Arabic content
- Email with English content
- Email with mixed Arabic/English content
- Email with vague date
- Email with missing time
- Email with online meeting link

==================================================
8. IMPROVE EVENT REVIEW BEFORE SAVING
==================================================

After AI extraction, the app should allow the user to review the event before saving.

Add or improve a review screen/card:
- Extracted title
- Date
- Time
- Course
- Location/link
- Notes
- Source email
- Confidence level
- Edit button
- Confirm/save button
- Cancel button

If confidence is low, show:
“Please review this event before saving.”

==================================================
9. IMPROVE APP FEEDBACK AND USER TRUST
==================================================

The user must always know what is happening.

Add proper feedback for:
- Loading
- Extracting
- Saving
- Syncing
- Failed connection
- AI failed
- No email detected
- No event found
- Event saved
- Event updated
- Event deleted

Avoid silent failures.

Do not show technical errors directly to students unless needed. Convert them into clear human-readable messages.

==================================================
10. TEST ALL NAVIGATION AND SCREENS
==================================================

Test every screen and navigation path:
- Login
- Register
- Home
- Schedule/calendar
- Event list
- Event details
- Add event
- Edit event
- Delete event
- AI extracted event review
- Settings
- Theme switch
- Language switch
- Empty schedule
- Error screens
- Extension connection screens

Fix:
- Broken navigation
- Back button issues
- Screen freezing
- Stuck modals
- Incorrect state updates
- Duplicate events
- Events not refreshing
- UI not updating after extraction
- App crash after rotation or reopening

==================================================
11. RESPONSIVE TESTING
==================================================

Test the Android app on:
- Small phone screen
- Medium phone screen
- Large phone screen
- Different font sizes
- Light mode
- Dark mode
- Arabic layout
- English layout

The app must not break when:
- Font size is increased
- Text is long
- Email subject is long
- Course name is long
- Date/time text is long
- Keyboard is open

==================================================
12. ACCESSIBILITY REQUIREMENTS
==================================================

Improve accessibility:
- All important text must have enough contrast.
- Buttons must be easy to tap.
- Touch targets should not be too small.
- Icons should have meaningful content descriptions where needed.
- Do not rely only on color to communicate status.
- Support readable font sizes.
- Make the UI comfortable for students using it daily.

==================================================
13. BACKEND AND API RELIABILITY
==================================================

Inspect and improve backend behavior:
- Validate incoming email extraction requests.
- Return clear success/error responses.
- Do not crash on empty email content.
- Do not crash on invalid AI response.
- Add safe parsing for AI JSON output.
- Add timeout handling.
- Add meaningful logs for development.
- Do not expose secrets.
- Ensure environment variables are documented.
- Ensure OpenRouter errors are handled properly.
- Ensure Supabase errors are handled properly.
- Ensure in-memory development mode still works.

==================================================
14. DATA QUALITY AND DUPLICATE PREVENTION
==================================================

Prevent duplicate events when the same email is extracted multiple times.

Possible approach:
- Store source email ID or hash.
- Compare event title + date + source email.
- Warn user if similar event already exists.
- Allow user to update existing event instead of creating duplicate.

==================================================
15. POLISH THE DESIGN SYSTEM
==================================================

Create or update a consistent design system:
- Color tokens
- Typography tokens
- Spacing tokens
- Card styles
- Button styles
- Input field styles
- Error/success/warning styles
- Light theme
- Dark theme

Use the required palette:
- #bcda4b
- #030301
- #f8fdf4

Design direction:
Calm, academic, clean, modern, student-friendly, not flashy.

==================================================
16. QUALITY ASSURANCE AND REAL TESTING
==================================================

Do not only claim that it works. Actually test it.

Run:
- Android build
- Backend server
- Extension loading
- API tests
- Manual UI testing
- Light/dark mode testing
- AI extraction testing
- End-to-end extraction from extension to app

Provide a final QA report that includes:
- What was broken
- What was fixed
- What was redesigned
- What was tested
- Screens tested
- Bugs found
- Bugs fixed
- Remaining limitations if any
- Screenshots or evidence if possible

==================================================
17. ACCEPTANCE CRITERIA
==================================================

The task is complete only when:

- The app looks professional and calm.
- Buttons are clearly visible.
- Text is readable everywhere.
- No text is hidden inside cards or frames.
- Light mode looks clean.
- Dark mode looks polished.
- Plus/add button bug is fixed.
- Modal/bottom sheet does not get stuck.
- AI extraction uses real email content.
- Different emails produce different extraction results.
- Extension clearly shows live connection and extraction status.
- Extracted details are visible to the user.
- Events successfully appear in the Android app.
- User can review/edit extracted events before saving.
- Errors are clear and user-friendly.
- Full end-to-end workflow is tested.
- The final result feels like a complete working application.

Do not stop at surface-level changes. Inspect the code, run the app, test the features, fix the root causes, and polish the full product.