import json
import logging
import httpx
from datetime import datetime
from pydantic import BaseModel, Field

from app.core.config import Settings
from app.schemas.event import ExtractedEvent

logger = logging.getLogger(__name__)

VERIFICATION_SYSTEM_PROMPT = """You are an academic schedule verification assistant.
Review the extracted event against the student's enrolled courses and weekly study schedule, and decide how it should be handled.

Return a valid JSON object only. Do not add markdown or explanations.
The JSON must contain:
{
  "action": "auto_add" | "needs_review" | "not_matching",
  "reason": "Clear explanation in the student's preferred language (Arabic or English)"
}

Rules:
1. "auto_add": Choose this ONLY if:
   - The event belongs to one of the student's enrolled courses (course code or name match).
   - AND it aligns with the course's teaching plan/syllabus (e.g. mentions that topic/assignment/exam around that week) OR with the weekly study schedule (happens during class days/times).
2. "needs_review": Choose this if:
   - The event belongs to an enrolled course.
   - BUT the details (dates, times, topic) are not clearly matching, or there is a discrepancy.
3. "not_matching": Choose this if:
   - The event belongs to a course the student is not enrolled in, or is completely unrelated.
"""


class VerificationResult(BaseModel):
    action: str = Field(pattern=r"^(auto_add|needs_review|not_matching)$")
    reason: str = Field(min_length=1, max_length=1000)


class VerificationService:
    def __init__(self, settings: Settings, transport: httpx.AsyncBaseTransport | None = None) -> None:
        self.settings = settings
        self.transport = transport

    async def verify(
        self,
        event: ExtractedEvent,
        courses: list[dict],
        schedule: list[dict],
        preferred_language: str = "ar"
    ) -> VerificationResult:
        # Fallback/Mock behavior for tests and local development when OpenRouter is not configured
        if not self.settings.openrouter_api_key:
            return self._verify_fallback(event, courses, schedule, preferred_language)

        # Build prompt context
        courses_context = [
            {
                "course_code": c["course_code"],
                "course_name": c["course_name"],
                "teaching_plan": c.get("teaching_plan") or "No teaching plan provided."
            }
            for c in courses
        ]

        schedule_context = [
            {
                "course_code": s["course_code"],
                "day_of_week": s["day_of_week"],
                "start_time": str(s["start_time"]),
                "end_time": str(s["end_time"]),
                "location": s.get("location")
            }
            for s in schedule
        ]

        user_prompt = f"""Student Enrolled Courses:
{json.dumps(courses_context, ensure_ascii=False, indent=2)}

Student Weekly Schedule:
{json.dumps(schedule_context, ensure_ascii=False, indent=2)}

Extracted Event to Verify:
- Title: {event.title}
- Course Code: {event.course_code}
- Course Name: {event.course_name}
- Due Date: {event.due_date.isoformat()}
- Event Type: {event.event_type}
- Notes: {event.notes}
- Evidence: {event.evidence}

Preferred Language: {preferred_language}
Determine the action and write the reason in {"Arabic" if preferred_language == "ar" else "English"}."""

        headers = {
            "Authorization": f"Bearer {self.settings.openrouter_api_key}",
            "HTTP-Referer": self.settings.openrouter_http_referer,
            "X-Title": self.settings.openrouter_x_title,
        }

        async with httpx.AsyncClient(
            timeout=self.settings.openrouter_timeout_seconds,
            transport=self.transport,
        ) as client:
            try:
                response = await client.post(
                    "https://openrouter.ai/api/v1/chat/completions",
                    headers=headers,
                    json={
                        "model": self.settings.openrouter_model,
                        "temperature": 0,
                        "response_format": {"type": "json_object"},
                        "messages": [
                            {"role": "system", "content": VERIFICATION_SYSTEM_PROMPT},
                            {"role": "user", "content": user_prompt},
                        ],
                    },
                )
                response.raise_for_status()
                content = response.json()["choices"][0]["message"]["content"].strip()
                if content.startswith("```"):
                    content = content.strip("`")
                    if content.startswith("json"):
                        content = content[4:].lstrip()
                parsed = json.loads(content)
                return VerificationResult.model_validate(parsed)
            except Exception as exc:
                logger.error("AI verification failed, falling back to rule-based verification: %s", exc)
                return self._verify_fallback(event, courses, schedule, preferred_language)

    def _verify_fallback(
        self,
        event: ExtractedEvent,
        courses: list[dict],
        schedule: list[dict],
        preferred_language: str
    ) -> VerificationResult:
        if not courses:
            reason = (
                "لم تقم بتسجيل أي مقررات بعد. تمت الإضافة تلقائياً."
                if preferred_language == "ar"
                else "No registered courses. Auto-added by default."
            )
            return VerificationResult(action="auto_add", reason=reason)

        event_course_code = (event.course_code or "").strip().upper()
        event_course_name = (event.course_name or "").strip().lower()

        # Find matching course
        matched_course = None
        for c in courses:
            c_code = c["course_code"].strip().upper()
            c_name = c["course_name"].strip().lower()
            if event_course_code and event_course_code == c_code:
                matched_course = c
                break
            if event_course_name and (event_course_name in c_name or c_name in event_course_name):
                matched_course = c
                break

        if not matched_course:
            reason = (
                f"المقرر {event.course_code or event.course_name or ''} غير موجود في مقرراتك المسجلة."
                if preferred_language == "ar"
                else f"Course {event.course_code or event.course_name or ''} is not in your registered courses."
            )
            return VerificationResult(action="not_matching", reason=reason)

        # Course matches. Check weekly schedule matching.
        course_code = matched_course["course_code"]
        
        # Check schedule
        has_schedule_slot = False
        for s in schedule:
            if s["course_code"].strip().upper() == course_code.strip().upper():
                has_schedule_slot = True
                break

        if has_schedule_slot:
            # Check day of week alignment
            # due_date is datetime, find weekday name in English
            # Python weekday(): Monday is 0, Sunday is 6
            days_mapping = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
            event_day = days_mapping[event.due_date.weekday()]
            
            day_matches = False
            for s in schedule:
                if s["course_code"].strip().upper() == course_code.strip().upper() and s["day_of_week"] == event_day:
                    day_matches = True
                    break

            if day_matches:
                reason = (
                    f"تمت المطابقة التلقائية: الحدث يخص مقرر {course_code} ويتطابق مع أيام محاضراتك."
                    if preferred_language == "ar"
                    else f"Auto-matched: The event belongs to course {course_code} and aligns with your class days."
                )
                return VerificationResult(action="auto_add", reason=reason)
            else:
                reason = (
                    f"يتطابق مع مقرر {course_code} ولكن في يوم آخر غير المعتاد لمكحاضراتك."
                    if preferred_language == "ar"
                    else f"Belongs to course {course_code} but on a different day than your scheduled classes."
                )
                return VerificationResult(action="needs_review", reason=reason)

        # Course matches but no schedule is defined, let's auto_add or needs_review.
        # If teaching plan contains keywords like midterm or homework, and it's in the text:
        teaching_plan = (matched_course.get("teaching_plan") or "").lower()
        event_title = event.title.lower()
        
        if event_title in teaching_plan or any(kw in event_title for kw in ["exam", "test", "quiz", "homework", "assignment", "واجب", "اختبار"]):
            reason = (
                f"تمت المطابقة مع مقرر {course_code} والخطة التعليمية للمقرر."
                if preferred_language == "ar"
                else f"Matched with course {course_code} and its teaching plan."
            )
            return VerificationResult(action="auto_add", reason=reason)

        reason = (
            f"يخص مقرر {course_code} ولكن التفاصيل تحتاج إلى مراجعة منك."
            if preferred_language == "ar"
            else f"Belongs to course {course_code} but details need review."
        )
        return VerificationResult(action="needs_review", reason=reason)
