-- RLS Policy Verification Script for Miyad
-- This script runs a test suite inside a transaction and rolls it back,
-- verifying that users are isolated and cannot access or modify each other's data.

BEGIN;

-- 1. Ensure mock auth schema and uid() function exist for simulation
CREATE SCHEMA IF NOT EXISTS auth;
CREATE OR REPLACE FUNCTION auth.uid() RETURNS UUID AS $$
  SELECT COALESCE(
    current_setting('request.jwt.claim.sub', true),
    NULLIF(current_setting('request.jwt.claims', true)::jsonb->>'sub', '')
  )::UUID;
$$ LANGUAGE SQL STABLE;

-- 2. Create two test users
INSERT INTO users (id, email, password_hash, name, university)
VALUES 
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'student_a@example.com', 'hashA', 'Student A', 'Univ A'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'student_b@example.com', 'hashB', 'Student B', 'Univ B');

-- Set up settings for both
INSERT INTO user_settings (user_id, preferred_language)
VALUES 
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'en'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'ar');

-- 3. Perform tests as Student A
SELECT '--- SIMULATING STUDENT A ---' AS test_phase;
SET LOCAL request.jwt.claim.sub = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';

-- Student A inserts an email hash and event
INSERT INTO email_hashes (user_id, hash, subject)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'hash-123456', 'English Assignment');

INSERT INTO events (user_id, title, event_type, due_date, source_hash)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'A Assignment', 'deadline', NOW() + INTERVAL '5 days', 'hash-123456');

-- Verify Student A can select their own settings and events
SELECT title FROM events WHERE user_id = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';
SELECT preferred_language FROM user_settings WHERE user_id = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';


-- 4. Perform tests as Student B
SELECT '--- SIMULATING STUDENT B (ISOLATION TEST) ---' AS test_phase;
SET LOCAL request.jwt.claim.sub = 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb';

-- Student B inserts their own email hash (same hash value, to test multi-user isolation on duplicates)
-- This should pass because the primary key is (user_id, hash)
INSERT INTO email_hashes (user_id, hash, subject)
VALUES ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'hash-123456', 'English Assignment');

INSERT INTO events (user_id, title, event_type, due_date, source_hash)
VALUES ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'B Assignment', 'deadline', NOW() + INTERVAL '5 days', 'hash-123456');

-- Verify Student B cannot see Student A's events (Should return 0 rows under RLS)
SELECT COUNT(*) FROM events WHERE user_id = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';

-- Verify Student B cannot see Student A's settings (Should return 0 rows)
SELECT COUNT(*) FROM user_settings WHERE user_id = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';

-- Verify Student B cannot update Student A's events (Should affect 0 rows)
UPDATE events SET title = 'Hacked' WHERE user_id = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';
SELECT title FROM events WHERE user_id = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';

-- Verify Student B cannot delete Student A's events (Should affect 0 rows)
DELETE FROM events WHERE user_id = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';

-- Try to insert event as Student B but forging Student A's user_id (Should FAIL policy check)
-- Wrap in savepoint to catch error and continue
SAVEPOINT forged_insert_savepoint;
INSERT INTO events (user_id, title, event_type, due_date)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Forged Event', 'deadline', NOW());
-- We expect an RLS check failure, so we handle it:
-- Note: If we get here, it means RLS didn't block it (which is a failure)
SELECT 'ERROR: Forged insert succeeded!' AS result;
ROLLBACK TO SAVEPOINT forged_insert_savepoint;


-- 5. Rollback transaction to clean up mock data
ROLLBACK;
SELECT 'RLS verification completed and rolled back successfully.' AS status;
