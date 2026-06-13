import json
from abc import ABC, abstractmethod

import httpx
from pydantic import BaseModel, ValidationError

from app.core.config import Settings
from app.schemas.email import EmailMetadata
from app.schemas.event import ExtractedEvent


SYSTEM_PROMPT = """You are an academic schedule extraction assistant.
Your ONLY job is to read university email content and extract academic events.
You MUST return ONLY valid JSON. No explanation. No markdown. No preamble.
If no academic events are found, return: {"events": []}
All datetime values MUST be strict ISO 8601 with the student's provided local timezone.
Never assume UTC. Omit events whose date cannot be resolved reliably."""


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
            "\nYour previous response was invalid. Return one JSON object only, "
            "with timezone-aware ISO 8601 dates and no markdown."
            if strict
            else ""
        )
        return f"""Extract all academic deadlines, exams, quizzes, assignments, lectures, and events.

Email Subject: {metadata.subject}
Email Sender: {metadata.sender}
Email Date: {metadata.timestamp.isoformat()}
Student Local Timezone: {metadata.timezone}

Email Body:
---
{raw_content}
---

Return exactly:
{{"events":[{{"title":"string","course_code":null,"event_type":"exam | deadline | quiz | lecture | other","due_date":"ISO 8601 with timezone","location":null,"notes":null}}]}}
{retry_note}"""

    async def extract(
        self, metadata: EmailMetadata, raw_content: str
    ) -> list[ExtractedEvent]:
        if not self.settings.openrouter_api_key:
            raise RuntimeError("OPENROUTER_API_KEY is not configured")
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
                content = response.json()["choices"][0]["message"]["content"].strip()
                if content.startswith("```"):
                    content = content.strip("`")
                    if content.startswith("json"):
                        content = content[4:].lstrip()
                try:
                    parsed = ExtractionEnvelope.model_validate(json.loads(content))
                    return parsed.events
                except (json.JSONDecodeError, ValidationError, KeyError) as exc:
                    last_error = exc
            raise ValueError("AI returned invalid event JSON after retry") from last_error


class FakeExtractor(Extractor):
    async def extract(
        self, metadata: EmailMetadata, raw_content: str
    ) -> list[ExtractedEvent]:
        if "NO_EVENT" in raw_content:
            return []
        is_arabic = "اختبار" in raw_content or "midterm" not in raw_content.lower()
        from datetime import datetime
        return [
            ExtractedEvent(
                title="اختبار منتصف الفصل" if is_arabic else "Database Assignment",
                course_code="MATH301" if is_arabic else "DBS201",
                event_type="exam" if is_arabic else "deadline",
                due_date=datetime.fromisoformat(
                    "2026-06-16T13:00:00+08:00"
                    if is_arabic
                    else "2026-06-12T23:59:00+08:00"
                ),
                location="القاعة 4-B" if is_arabic else "LMS / Online",
                notes=(
                    "إحضار الآلة الحاسبة والبطاقة الجامعية."
                    if is_arabic
                    else "Late submissions will not be accepted."
                ),
            )
        ]
