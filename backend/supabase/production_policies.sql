-- Miyad Production RLS Policies
-- This file defines policies to restrict access on all tables so that authenticated users
-- can only read, write, update, and delete their own rows.

-- 1. Users table policies
CREATE POLICY users_select_policy ON users
    FOR SELECT TO authenticated USING (auth.uid() = id);

CREATE POLICY users_insert_policy ON users
    FOR INSERT TO authenticated WITH CHECK (auth.uid() = id);

CREATE POLICY users_update_policy ON users
    FOR UPDATE TO authenticated USING (auth.uid() = id) WITH CHECK (auth.uid() = id);

CREATE POLICY users_delete_policy ON users
    FOR DELETE TO authenticated USING (auth.uid() = id);


-- 2. Email Hashes table policies
CREATE POLICY email_hashes_select_policy ON email_hashes
    FOR SELECT TO authenticated USING (auth.uid() = user_id);

CREATE POLICY email_hashes_insert_policy ON email_hashes
    FOR INSERT TO authenticated WITH CHECK (auth.uid() = user_id);

CREATE POLICY email_hashes_update_policy ON email_hashes
    FOR UPDATE TO authenticated USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

CREATE POLICY email_hashes_delete_policy ON email_hashes
    FOR DELETE TO authenticated USING (auth.uid() = user_id);


-- 3. Events table policies
CREATE POLICY events_select_policy ON events
    FOR SELECT TO authenticated USING (auth.uid() = user_id);

CREATE POLICY events_insert_policy ON events
    FOR INSERT TO authenticated WITH CHECK (auth.uid() = user_id);

CREATE POLICY events_update_policy ON events
    FOR UPDATE TO authenticated USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

CREATE POLICY events_delete_policy ON events
    FOR DELETE TO authenticated USING (auth.uid() = user_id);


-- 4. Notification Tokens table policies
CREATE POLICY notification_tokens_select_policy ON notification_tokens
    FOR SELECT TO authenticated USING (auth.uid() = user_id);

CREATE POLICY notification_tokens_insert_policy ON notification_tokens
    FOR INSERT TO authenticated WITH CHECK (auth.uid() = user_id);

CREATE POLICY notification_tokens_update_policy ON notification_tokens
    FOR UPDATE TO authenticated USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

CREATE POLICY notification_tokens_delete_policy ON notification_tokens
    FOR DELETE TO authenticated USING (auth.uid() = user_id);


-- 5. User Settings table policies
CREATE POLICY user_settings_select_policy ON user_settings
    FOR SELECT TO authenticated USING (auth.uid() = user_id);

CREATE POLICY user_settings_insert_policy ON user_settings
    FOR INSERT TO authenticated WITH CHECK (auth.uid() = user_id);

CREATE POLICY user_settings_update_policy ON user_settings
    FOR UPDATE TO authenticated USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

CREATE POLICY user_settings_delete_policy ON user_settings
    FOR DELETE TO authenticated USING (auth.uid() = user_id);
