# Miyad Test Plan

This document outlines the test strategy, test suites, and boundary check scenarios to verify the Miyad scheduler assistant.

## 1. Test Strategy

Miyad utilizes a multi-layer verification strategy to ensure reliability across the three core modules:

```
                  ┌──────────────────────┐
                  │   Android App (JVM)   │
                  │   - Compose layouts  │
                  │   - Local alarms     │
                  └──────────┬───────────┘
                             │ API Calls (Retrofit)
                             ▼
 ┌────────────────┐       ┌──────────────────────┐
 │ OWA Extension  ├──────►│   FastAPI Backend    │
 │ - MV3 Scrapers │       │   - JWT Auth, DB RPC │
 │ - Local hashes │       │   - Gemma AI Parser  │
 └────────────────┘       └──────────────────────┘
```

---

## 2. Test Suites Overview

### 2.1 Backend API Unit Tests (`pytest`)
* **Location:** `backend/tests/`
* **Focus:**
  * Endpoint health status checking.
  * Register / Login validations and credentials encryption.
  * Setting configurations read/write and extension connection heartbeat.
  * Email scraper webhook processing, duplicate fingerprint rejection, and DB insertions.
  * Local/Supabase mock storage validation and atomic RPC save.
  * Invalid event configurations (end date prior to start date validation).
  * Multilingual payload parsing.

### 2.2 Chrome Extension Unit Tests (`npm test`)
* **Location:** `extension/tests/`
* **Focus:**
  * MV3 compliance structure parsing.
  * Stable SHA-256 fingerprint generation using subject + sender + timestamp + body slice.
  * Preference local storage operations and UI localization key synchronization.
  * Network retry scheduler queues with exponential backoff timers.
  * Scraper DOM element matching and fallback selector queries.

### 2.3 Android JVM Unit Tests (`gradlew test`)
* **Location:** `frontend/app/src/test/java/`
* **Focus:**
  * Core screen ViewModels state flow updates.
  * Input validations (auth fields formatting, timezone configurations).
  * Date matrix and calendar week grid logic.
  * Local alarm scheduler planning calculations.

---

## 3. Boundary & Security Check Scenarios

The following scenarios are verified to enforce security parameters and database reliability:

### 3.1 Authentication & Isolation
* **Test Case SEC-01:** Requesting a protected route without a JWT token must return `401 Unauthorized`.
* **Test Case SEC-02:** Logging in with incorrect credentials must return `401 Unauthorized`.
* **Test Case SEC-03:** Deleting another user's event must return `404 Not Found` (multi-tenant boundary check).

### 3.2 Input Validation
* **Test Case VAL-01:** Creating an event where the start time is after the end time must return `422 Unprocessable Entity`.
* **Test Case VAL-02:** Sending a malformed hash structure (non-hex characters or wrong length) to `/api/process-email` must trigger validation exception `422`.
* **Test Case VAL-03:** AI extraction payloads omitting timezone metadata must trigger retry validation and raise ValueError.

### 3.3 Deduplication & Processing
* **Test Case DUP-01:** Sending the same email scraper payload twice must return status `already_processed` with zero events created.
* **Test Case DUP-02:** Running the manual extraction with `save=False` must return a preview array of events without inserting rows in the database.
* **Test Case DUP-03:** Email messages containing no academic deadlines must return `no_events_found` and record the fingerprint.
