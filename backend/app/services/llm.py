from datetime import datetime
import json
import logging
from abc import ABC, abstractmethod

import httpx
from pydantic import BaseModel, ValidationError

from app.core.config import Settings
from app.schemas.email import EmailMetadata
from app.schemas.event import ExtractedEvent


logger = logging.getLogger(__name__)

SYSTEM_PROMPT = """You are an academic schedule extraction assistant.
Read the supplied university email and extract only real academic events.
Return one valid JSON object only. Do not add markdown or explanations.
If no academic event with a reliably resolvable date exists, return {"events":[]}.
All datetime values must be strict ISO 8601 in the student's provided timezone.
Never invent a date, time, course, lecturer, location, or generic event."""


class ExtractionEnvelope(BaseModel):
    events: list[ExtractedEvent]


class Extractor(ABC):
    @abstractmethod
    async def extract(
        self, metadata: EmailMetadata, raw_content: str
    ) -> list[ExtractedEvent]: ...


class OpenRouterExtractor(Extractor):
    def __init__(
        self,
        settings: Settings,
        transport: httpx.AsyncBaseTransport | None = None,
    ) -> None:
        self.settings = settings
        self.transport = transport

    def _user_prompt(
        self, metadata: EmailMetadata, raw_content: str, strict: bool = False
    ) -> str:
        retry_note = (
            "\nYour previous response was invalid. Return the JSON object only "
            "and preserve timezone-aware ISO 8601 dates."
            if strict
            else ""
        )
        return f"""Extract deadlines, exams, quizzes, assignments, lectures, cancellations, and meetings.

Email Subject: {metadata.subject}
Email Sender: {metadata.sender}
Email Date: {metadata.timestamp.isoformat()}
Student Local Timezone: {metadata.timezone}

Email Body:
---
{raw_content}
---

Rules:
- Use only facts supported by this email.
- If a vague date cannot be resolved from the email timestamp, omit the event.
- If a calendar date is explicit but no time is supplied, use local midnight and
  set all_day to true. Never guess a clock time.
- Evidence must be the shortest supporting phrase from the email.
- Confidence is high when the event and date are explicit, medium for a safe
  inference, and low when the student must review an ambiguity.
- Copy the source subject and sender into every event.

Return exactly this shape:
{{"events":[{{"title":"string","course_code":null,"course_name":null,
"assignment_name":null,"event_type":"exam | deadline | quiz | lecture | other",
"due_date":"ISO 8601 with timezone","all_day":false,"location":null,
"lecturer":null,"notes":null,
"confidence":"high | medium | low","source_email_subject":"string",
"source_email_sender":"string","evidence":"short supporting text"}}]}}
{retry_note}"""

    async def extract(
        self, metadata: EmailMetadata, raw_content: str
    ) -> list[ExtractedEvent]:
        if not self.settings.openrouter_api_key:
            raise RuntimeError(
                "AI extraction is not configured. Set OPENROUTER_API_KEY on the backend."
            )
        headers = {
            "Authorization": f"Bearer {self.settings.openrouter_api_key}",
            "HTTP-Referer": self.settings.openrouter_http_referer,
            "X-Title": self.settings.openrouter_x_title,
        }
        async with httpx.AsyncClient(
            timeout=self.settings.openrouter_timeout_seconds,
            transport=self.transport,
        ) as client:
            last_error: Exception | None = None
            for attempt in range(2):
                response = await client.post(
                    "https://openrouter.ai/api/v1/chat/completions",
                    headers=headers,
                    json={
                        "model": self.settings.openrouter_model,
                        "temperature": 0,
                        "response_format": {"type": "json_object"},
                        "messages": [
                            {"role": "system", "content": SYSTEM_PROMPT},
                            {
                                "role": "user",
                                "content": self._user_prompt(
                                    metadata, raw_content, strict=attempt == 1
                                ),
                            },
                        ],
                    },
                )
                response.raise_for_status()
                try:
                    content = response.json()["choices"][0]["message"]["content"].strip()
                except (KeyError, IndexError, TypeError, AttributeError) as exc:
                    last_error = exc
                    logger.warning("OpenRouter returned an unexpected response envelope")
                    continue
                if content.startswith("```"):
                    content = content.strip("`")
                    if content.startswith("json"):
                        content = content[4:].lstrip()
                try:
                    parsed = ExtractionEnvelope.model_validate(json.loads(content))
                    return [
                        event.model_copy(
                            update={
                                "source_email_subject": (
                                    event.source_email_subject or metadata.subject
                                ),
                                "source_email_sender": (
                                    event.source_email_sender or metadata.sender
                                ),
                            }
                        )
                        for event in parsed.events
                    ]
                except (json.JSONDecodeError, ValidationError, KeyError) as exc:
                    last_error = exc
                    logger.warning(
                        "OpenRouter returned invalid extraction JSON (attempt %s)",
                        attempt + 1,
                    )
            raise ValueError("AI returned invalid event JSON after retry") from last_error


class UnavailableExtractor(Extractor):
    async def extract(
        self, metadata: EmailMetadata, raw_content: str
    ) -> list[ExtractedEvent]:
        raise RuntimeError(
            "AI extraction is not configured. Set OPENROUTER_API_KEY on the backend."
        )


class FakeExtractor(Extractor):
    async def extract(
        self, metadata: EmailMetadata, raw_content: str
    ) -> list[ExtractedEvent]:
        if "NO_EVENT" in raw_content:
            return []
        is_arabic = "اختبار" in raw_content or "موعد" in raw_content or "سيكون" in raw_content
        is_all_day = "ALL_DAY" in raw_content or "all day" in raw_content.lower() or "18 June" in raw_content
        
        # Determine dynamic details based on keywords
        title = "Database Assignment"
        course_code = "DBS201"
        event_type = "deadline"
        location = "LMS / Online"
        notes = "Late submissions will not be accepted."
        
        if is_arabic:
            title = "اختبار منتصف الفصل"
            course_code = "MATH301"
            event_type = "exam"
            location = "القاعة 4-B"
            notes = "إحضار الآلة الحاسبة والبطاقة الجامعية."
        elif "final exam" in raw_content.lower() or "الاختبار النهائي" in raw_content:
            title = "Final Exam"
            course_code = "CS101"
            event_type = "exam"
            location = "Hall A"
            notes = "Please bring your ID."
        
        # Calculate dynamic due date
        if is_all_day:
            due_date_str = "2026-06-18T00:00:00+08:00"
        elif "25 June 2026" in raw_content or "25 يونيو 2026" in raw_content:
            due_date_str = "2026-06-25T09:00:00+08:00"
        else:
            due_date_str = "2026-06-16T13:00:00+08:00" if is_arabic else "2026-06-12T23:59:00+08:00"
            
        return [
            ExtractedEvent(
                title=title,
                course_code=course_code,
                event_type=event_type,
                due_date=datetime.fromisoformat(due_date_str),
                all_day=is_all_day,
                location=location,
                notes=notes,
                confidence="high",
                source_email_subject=metadata.subject,
                source_email_sender=metadata.sender,
                evidence="Mock evidence extract",
            )
        ]

