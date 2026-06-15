import asyncio
import json

import httpx
import pytest

from app.core.config import Settings
from app.schemas.email import EmailMetadata
from app.services.llm import OpenRouterExtractor


def metadata() -> EmailMetadata:
    return EmailMetadata(
        sender="lecturer@university.edu",
        subject="Database assignment",
        timestamp="2026-06-11T10:00:00+08:00",
        timezone="Asia/Kuala_Lumpur",
    )


def settings() -> Settings:
    return Settings(
        environment="test",
        database_backend="memory",
        jwt_secret="test-secret-that-is-long-enough",
        openrouter_api_key="test-key",
    )


def response(content: str) -> httpx.Response:
    return httpx.Response(
        200,
        json={"choices": [{"message": {"content": content}}]},
    )


def test_retries_once_after_malformed_json() -> None:
    requests: list[dict] = []
    replies = iter(
        [
            response("not json"),
            response(
                json.dumps(
                    {
                        "events": [
                            {
                                "title": "Database Assignment",
                                "course_code": "DBS201",
                                "event_type": "deadline",
                                "due_date": "2026-06-12T23:59:00+08:00",
                                "location": "LMS / Online",
                                "notes": None,
                            }
                        ]
                    }
                )
            ),
        ]
    )

    def handler(request: httpx.Request) -> httpx.Response:
        requests.append(json.loads(request.content))
        return next(replies)

    extractor = OpenRouterExtractor(settings(), httpx.MockTransport(handler))
    events = asyncio.run(extractor.extract(metadata(), "Submit by Friday."))

    assert len(events) == 1
    assert len(requests) == 2
    assert "previous response was invalid" in requests[1]["messages"][1]["content"]


def test_rejects_invalid_json_after_single_retry() -> None:
    attempts = 0

    def handler(_request: httpx.Request) -> httpx.Response:
        nonlocal attempts
        attempts += 1
        return response("{invalid")

    extractor = OpenRouterExtractor(settings(), httpx.MockTransport(handler))
    with pytest.raises(ValueError, match="invalid event JSON"):
        asyncio.run(extractor.extract(metadata(), "Submit tomorrow."))
    assert attempts == 2


def test_rejects_dates_without_timezone_after_single_retry() -> None:
    content = json.dumps(
        {
            "events": [
                {
                    "title": "Quiz",
                    "course_code": None,
                    "event_type": "quiz",
                    "due_date": "2026-06-12T10:00:00",
                    "location": None,
                    "notes": None,
                }
            ]
        }
    )
    attempts = 0

    def handler(_request: httpx.Request) -> httpx.Response:
        nonlocal attempts
        attempts += 1
        return response(content)

    extractor = OpenRouterExtractor(settings(), httpx.MockTransport(handler))
    with pytest.raises(ValueError, match="invalid event JSON"):
        asyncio.run(extractor.extract(metadata(), "Quiz tomorrow."))
    assert attempts == 2


def test_real_email_body_and_source_metadata_are_sent_for_each_request() -> None:
    prompts: list[str] = []
    replies = iter([
        response(json.dumps({
            "events": [{
                "title": "Assignment 2",
                "course_code": "DBS201",
                "event_type": "deadline",
                "due_date": "2026-06-20T23:59:00+08:00",
                "location": "LMS",
                "notes": None,
                "confidence": "high",
                "evidence": "Assignment 2 is due 20 June at 11:59 PM."
            }]
        })),
        response(json.dumps({
            "events": [{
                "title": "Final exam",
                "course_code": "DBS201",
                "event_type": "exam",
                "due_date": "2026-06-25T09:00:00+08:00",
                "location": "Hall A",
                "notes": None,
                "confidence": "high",
                "evidence": "The final exam is 25 June at 9 AM in Hall A."
            }]
        })),
    ])

    def handler(request: httpx.Request) -> httpx.Response:
        body = json.loads(request.content)
        prompts.append(body["messages"][1]["content"])
        return next(replies)

    extractor = OpenRouterExtractor(settings(), httpx.MockTransport(handler))
    first = asyncio.run(
        extractor.extract(metadata(), "Assignment 2 is due 20 June at 11:59 PM.")
    )
    second = asyncio.run(
        extractor.extract(metadata(), "The final exam is 25 June at 9 AM in Hall A.")
    )

    assert first[0].title != second[0].title
    assert "Assignment 2 is due" in prompts[0]
    assert "final exam is 25 June" in prompts[1]
    assert first[0].source_email_subject == metadata().subject
    assert first[0].source_email_sender == metadata().sender


@pytest.mark.parametrize(
    ("email_body", "event_payload", "expected_type", "expected_count", "all_day"),
    [
        (
            "Assignment 3 is due 20 June at 11:59 PM.",
            {
                "title": "Assignment 3",
                "event_type": "deadline",
                "due_date": "2026-06-20T23:59:00+08:00",
            },
            "deadline",
            1,
            False,
        ),
        (
            "الاختبار النهائي يوم 25 يونيو الساعة 9 صباحاً.",
            {
                "title": "الاختبار النهائي",
                "event_type": "exam",
                "due_date": "2026-06-25T09:00:00+08:00",
            },
            "exam",
            1,
            False,
        ),
        (
            "Class cancelled: محاضرة قواعد البيانات on 22 June at 2 PM.",
            {
                "title": "Database lecture cancelled",
                "event_type": "other",
                "due_date": "2026-06-22T14:00:00+08:00",
            },
            "other",
            1,
            False,
        ),
        (
            "Project meeting on 23 June at 4 PM, Teams link attached.",
            {
                "title": "Project meeting",
                "event_type": "other",
                "due_date": "2026-06-23T16:00:00+08:00",
                "location": "Teams",
            },
            "other",
            1,
            False,
        ),
        (
            "Assignment feedback is now available. No new deadline.",
            None,
            None,
            0,
            False,
        ),
        (
            "Lab report is due on 18 June.",
            {
                "title": "Lab report",
                "event_type": "deadline",
                "due_date": "2026-06-18T00:00:00+08:00",
                "all_day": True,
            },
            "deadline",
            1,
            True,
        ),
        (
            "The quiz will happen sometime later this term.",
            None,
            None,
            0,
            False,
        ),
    ],
)
def test_academic_email_matrix_contract(
    email_body: str,
    event_payload: dict | None,
    expected_type: str | None,
    expected_count: int,
    all_day: bool,
) -> None:
    captured_prompt = ""
    payload = {"events": []}
    if event_payload is not None:
        payload["events"] = [{
            "course_code": None,
            "location": None,
            "notes": None,
            **event_payload,
        }]

    def handler(request: httpx.Request) -> httpx.Response:
        nonlocal captured_prompt
        captured_prompt = json.loads(request.content)["messages"][1]["content"]
        return response(json.dumps(payload, ensure_ascii=False))

    extractor = OpenRouterExtractor(settings(), httpx.MockTransport(handler))
    events = asyncio.run(extractor.extract(metadata(), email_body))

    assert email_body in captured_prompt
    assert len(events) == expected_count
    if events:
        assert events[0].event_type == expected_type
        assert events[0].all_day is all_day
