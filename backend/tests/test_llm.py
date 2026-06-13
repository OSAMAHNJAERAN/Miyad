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
