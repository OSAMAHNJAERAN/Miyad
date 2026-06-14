CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    name TEXT,
    university TEXT,
    preferred_language TEXT NOT NULL DEFAULT 'ar'
        CHECK (preferred_language IN ('ar', 'en')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

CREATE TABLE IF NOT EXISTS email_hashes (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    hash TEXT NOT NULL,
    subject TEXT,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, hash)
);

CREATE INDEX IF NOT EXISTS idx_email_hashes_user ON email_hashes(user_id);
CREATE INDEX IF NOT EXISTS idx_email_hashes_hash ON email_hashes(hash);

CREATE TABLE IF NOT EXISTS events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    course_code TEXT,
    event_type TEXT NOT NULL
        CHECK (event_type IN ('exam', 'deadline', 'quiz', 'lecture', 'other')),
    due_date TIMESTAMPTZ NOT NULL,
    location TEXT,
    notes TEXT,
    description TEXT,
    start_time TIMESTAMPTZ,
    end_time TIMESTAMPTZ,
    all_day BOOLEAN NOT NULL DEFAULT FALSE,
    repeat TEXT NOT NULL DEFAULT 'none'
        CHECK (repeat IN ('none', 'daily', 'weekly', 'monthly', 'custom')),
    event_color TEXT NOT NULL DEFAULT '#B8F23A'
        CHECK (event_color ~ '^#[0-9A-Fa-f]{6}$'),
    source_hash TEXT,
    reminder TEXT DEFAULT 'one_day'
        CHECK (reminder IN ('same_day', 'one_day', 'one_week', 'none')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT events_source_hash_fk
        FOREIGN KEY (user_id, source_hash)
        REFERENCES email_hashes(user_id, hash)
        ON DELETE NO ACTION
);

CREATE INDEX IF NOT EXISTS idx_events_user_date ON events(user_id, due_date);

ALTER TABLE events ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE events ADD COLUMN IF NOT EXISTS start_time TIMESTAMPTZ;
ALTER TABLE events ADD COLUMN IF NOT EXISTS end_time TIMESTAMPTZ;
ALTER TABLE events ADD COLUMN IF NOT EXISTS all_day BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE events ADD COLUMN IF NOT EXISTS repeat TEXT NOT NULL DEFAULT 'none';
ALTER TABLE events ADD COLUMN IF NOT EXISTS event_color TEXT NOT NULL DEFAULT '#B8F23A';

UPDATE events
SET
    description = COALESCE(description, notes),
    start_time = COALESCE(start_time, due_date),
    end_time = COALESCE(end_time, due_date + INTERVAL '1 hour')
WHERE start_time IS NULL OR end_time IS NULL;

ALTER TABLE events
    DROP CONSTRAINT IF EXISTS events_repeat_check;
ALTER TABLE events
    ADD CONSTRAINT events_repeat_check
    CHECK (repeat IN ('none', 'daily', 'weekly', 'monthly', 'custom'));
ALTER TABLE events
    DROP CONSTRAINT IF EXISTS events_event_color_check;
ALTER TABLE events
    ADD CONSTRAINT events_event_color_check
    CHECK (event_color ~ '^#[0-9A-Fa-f]{6}$');

CREATE OR REPLACE FUNCTION save_extraction(
    p_user_id UUID,
    p_hash TEXT,
    p_subject TEXT,
    p_events JSONB
)
RETURNS SETOF events
LANGUAGE plpgsql
SET search_path = public
AS $$
DECLARE
    inserted_hashes INTEGER;
BEGIN
    INSERT INTO email_hashes (user_id, hash, subject)
    VALUES (p_user_id, p_hash, p_subject)
    ON CONFLICT (user_id, hash) DO NOTHING;

    GET DIAGNOSTICS inserted_hashes = ROW_COUNT;
    IF inserted_hashes = 0 OR jsonb_array_length(p_events) = 0 THEN
        RETURN;
    END IF;

    RETURN QUERY
    INSERT INTO events (
        user_id,
        title,
        course_code,
        event_type,
        due_date,
        location,
        notes,
        description,
        start_time,
        end_time,
        all_day,
        repeat,
        event_color,
        source_hash,
        reminder
    )
    SELECT
        p_user_id,
        item->>'title',
        NULLIF(item->>'course_code', ''),
        item->>'event_type',
        (item->>'due_date')::TIMESTAMPTZ,
        NULLIF(item->>'location', ''),
        NULLIF(item->>'notes', ''),
        NULLIF(item->>'notes', ''),
        (item->>'due_date')::TIMESTAMPTZ,
        (item->>'due_date')::TIMESTAMPTZ + INTERVAL '1 hour',
        FALSE,
        'none',
        '#B8F23A',
        p_hash,
        'one_day'
    FROM jsonb_array_elements(p_events) AS item
    RETURNING events.*;
END;
$$;

REVOKE ALL ON FUNCTION save_extraction(UUID, TEXT, TEXT, JSONB)
FROM PUBLIC, anon, authenticated;
GRANT EXECUTE ON FUNCTION save_extraction(UUID, TEXT, TEXT, JSONB)
TO service_role;

CREATE TABLE IF NOT EXISTS notification_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token TEXT UNIQUE NOT NULL,
    platform TEXT NOT NULL DEFAULT 'android',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS user_settings (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    preferred_language TEXT NOT NULL DEFAULT 'ar'
        CHECK (preferred_language IN ('ar', 'en')),
    notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    reminder_same_day BOOLEAN NOT NULL DEFAULT TRUE,
    reminder_one_day BOOLEAN NOT NULL DEFAULT TRUE,
    reminder_one_week BOOLEAN NOT NULL DEFAULT FALSE,
    extension_last_seen TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE user_settings
ADD COLUMN IF NOT EXISTS extension_last_seen TIMESTAMPTZ;

ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE email_hashes ENABLE ROW LEVEL SECURITY;
ALTER TABLE events ENABLE ROW LEVEL SECURITY;
ALTER TABLE notification_tokens ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_settings ENABLE ROW LEVEL SECURITY;
