# Miyad Chrome Extension

Zero-dependency Chrome Extension using Manifest V3. It observes only Outlook's
resolved reading pane, computes the required SHA-256 fingerprint, and sends the
currently visible message to the Miyad backend.

## Load in Chrome

1. Open `chrome://extensions`.
2. Enable Developer mode.
3. Choose **Load unpacked** and select this `extension` directory.
4. Open the popup, set the backend URL if needed, and log in or register.
5. Open a message in Outlook Web App.

The popup defaults to `http://127.0.0.1:8000`. Production deployments can use
`https://api.miyad.app`.

The popup probes `GET /health` and displays the configured backend origin. Use
**اختبار الاتصال** after changing the URL. If the local backend is not running
or a deployed URL is incorrect, the popup reports an actionable connection
message instead of the browser's generic `Failed to fetch`.

Authenticated popup activity sends a backend heartbeat so the Android app can
show whether the extension connected recently. Custom backend URLs request only
that origin through optional host permissions; unsafe URL schemes and embedded
credentials are rejected.

## Privacy and security

- The extension does not request or store Outlook credentials.
- It reads only the message already visible in the user's reading pane.
- AI keys never enter the extension.
- Local hashes prevent repeat requests; the backend also deduplicates per user.
- Failed network requests are retried with bounded exponential backoff.

## Test

```powershell
npm test
```
