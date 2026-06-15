-- 1. جدول المواد الدراسي (المقررات)
CREATE TABLE IF NOT EXISTS courses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    course_code TEXT NOT NULL,
    course_name TEXT NOT NULL,
    teaching_plan TEXT, -- نص الخطة التعليمية أو الـ Syllabus
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, course_code)
);

CREATE INDEX IF NOT EXISTS idx_courses_user ON courses(user_id);

-- 2. جدول الجدول الدراسي الأسبوعي (أوقات المحاضرات)
CREATE TABLE IF NOT EXISTS study_schedule (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    course_code TEXT NOT NULL,
    day_of_week TEXT NOT NULL CHECK (day_of_week IN ('Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday')),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    location TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_study_schedule_user ON study_schedule(user_id);

-- 3. جدول التحذيرات والأحداث المعلقة للمراجعة
CREATE TABLE IF NOT EXISTS verification_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_data JSONB NOT NULL,      -- بيانات الحدث المستخرج بالكامل
    email_hash TEXT NOT NULL,       -- هاش الإيميل المصدر
    alert_type TEXT NOT NULL CHECK (alert_type IN ('needs_review', 'not_matching')),
    ai_reason TEXT,                 -- سبب اختيار AI لهذا التصنيف
    confidence TEXT DEFAULT 'low',
    status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'confirmed', 'rejected')),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    resolved_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_verification_alerts_user ON verification_alerts(user_id);

-- تفعيل الحماية RLS للجداول الجديدة
ALTER TABLE courses ENABLE ROW LEVEL SECURITY;
ALTER TABLE study_schedule ENABLE ROW LEVEL SECURITY;
ALTER TABLE verification_alerts ENABLE ROW LEVEL SECURITY;

-- سياسات RLS
DROP POLICY IF EXISTS courses_policy ON courses;
CREATE POLICY courses_policy ON courses FOR ALL TO authenticated USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS study_schedule_policy ON study_schedule;
CREATE POLICY study_schedule_policy ON study_schedule FOR ALL TO authenticated USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS verification_alerts_policy ON verification_alerts;
CREATE POLICY verification_alerts_policy ON verification_alerts FOR ALL TO authenticated USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
