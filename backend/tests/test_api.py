from datetime import datetime, timedelta

from fastapi.testclient import TestClient

from app.core.config import Settings
from app.main import create_app
from app.schemas.email import EmailMetadata
from app.schemas.event import ExtractedEvent
from app.services.llm import Extractor
from app.services.storage import InMemoryStorage
from app.core.rate_limit import reset_rate_limiter


class FakeExtractor(Extractor):
    async def extract(
        self, metadata: EmailMetadata, raw_content: str
    ) -> list[ExtractedEvent]:
        if "NO_EVENT" in raw_content:
            return []
        is_arabic = "اختبار" in raw_content
        is_all_day = "ALL_DAY" in raw_content
        return [
            ExtractedEvent(
                title="اختبار منتصف الفصل" if is_arabic else "Database Assignment",
                course_code="MATH301" if is_arabic else "DBS201",
                event_type="exam" if is_arabic else "deadline",
                due_date=datetime.fromisoformat(
                    "2026-06-18T00:00:00+08:00"
                    if is_all_day
                    else (
                        "2026-06-16T13:00:00+08:00"
                        if is_arabic
                        else "2026-06-12T23:59:00+08:00"
                    )
                ),
                all_day=is_all_day,
                location="القاعة 4-B" if is_arabic else "LMS / Online",
                notes=(
                    "إحضار الآلة الحاسبة والبطاقة الجامعية."
                    if is_arabic
                    else "Late submissions will not be accepted."
                ),
            )
        ]


def make_client() -> tuple[TestClient, InMemoryStorage]:
    reset_rate_limiter()
    storage = InMemoryStorage()
    settings = Settings(
        environment="test",
        database_backend="memory",
        jwt_secret="test-secret-that-is-long-enough",
    )
    app = create_app(settings, storage, FakeExtractor())
    return TestClient(app), storage


def register(client: TestClient, email: str = "student@example.edu") -> dict:
    response = client.post(
        "/api/auth/register",
        json={
            "email": email,
            "password": "strong-password",
            "name": "Student",
            "university": "Example University",
        },
    )
    assert response.status_code == 201
    return response.json()


def test_health_auth_and_persistent_token() -> None:
    client, _ = make_client()
    with client:
        assert client.get("/health").json()["status"] == "ok"
        auth = register(client)
        assert auth["token"].count(".") == 2
        assert client.post(
            "/api/auth/login",
            json={"email": "student@example.edu", "password": "wrong"},
        ).status_code == 401
        login = client.post(
            "/api/auth/login",
            json={"email": "student@example.edu", "password": "strong-password"},
        )
        assert login.status_code == 200
        headers = {"Authorization": f"Bearer {auth['token']}"}
        assert client.get("/api/settings", headers=headers).json()[
            "extension_connected"
        ] is False
        assert client.post("/api/extension/heartbeat", headers=headers).json() == {
            "connected": True
        }
        assert client.get("/api/settings", headers=headers).json()[
            "extension_connected"
        ] is True


def test_process_email_dedup_events_patch_delete_and_no_raw_storage() -> None:
    client, storage = make_client()
    with client:
        token = register(client)["token"]
        headers = {"Authorization": f"Bearer {token}"}
        payload = {
            "metadata": {
                "sender": "lecturer@university.edu",
                "subject": "Database Assignment Submission",
                "timestamp": "2026-06-11T10:00:00+08:00",
                "timezone": "Asia/Kuala_Lumpur",
            },
            "raw_content": "Submit by Friday at 11:59 PM through the LMS.",
            "email_hash": "sha256:" + ("a" * 64),
        }
        first = client.post("/api/process-email", headers=headers, json=payload)
        assert first.status_code == 200
        assert first.json()["events_created"] == 1
        duplicate = client.post("/api/process-email", headers=headers, json=payload)
        assert duplicate.json()["status"] == "already_processed"

        events = client.get("/api/events", headers=headers).json()["events"]
        assert len(events) == 1
        event_id = events[0]["id"]
        patched = client.patch(
            f"/api/events/{event_id}",
            headers=headers,
            json={"location": "Online portal", "reminder": "one_week"},
        )
        assert patched.status_code == 200
        assert patched.json()["reminder"] == "one_week"
        assert client.delete(f"/api/events/{event_id}", headers=headers).json() == {
            "success": True
        }
        assert "raw_content" not in repr(storage.events)
        assert "Submit by Friday" not in repr(storage.__dict__)


def test_create_independent_calendar_event() -> None:
    client, _ = make_client()
    with client:
        token = register(client)["token"]
        headers = {"Authorization": f"Bearer {token}"}
        created = client.post(
            "/api/events",
            headers=headers,
            json={
                "title": "Study group",
                "description": "Review chapters 4 and 5",
                "start_time": "2026-06-20T14:00:00+08:00",
                "end_time": "2026-06-20T15:30:00+08:00",
                "all_day": False,
                "repeat": "weekly",
                "location": "Library room 2",
                "reminder": "same_day",
                "event_color": "#2388C9",
                "event_type": "other",
            },
        )
        assert created.status_code == 201
        event = created.json()
        assert event["due_date"] == "2026-06-20T14:00:00+08:00"
        assert event["start_time"] == "2026-06-20T14:00:00+08:00"
        assert event["end_time"] == "2026-06-20T15:30:00+08:00"
        assert event["repeat"] == "weekly"
        assert event["event_color"] == "#2388C9"

        updated = client.patch(
            f"/api/events/{event['id']}",
            headers=headers,
            json={
                "title": "Updated study group",
                "description": "Review chapters 6 and 7",
                "start_time": "2026-06-21T09:00:00+08:00",
                "end_time": "2026-06-21T11:00:00+08:00",
                "all_day": False,
                "repeat": "monthly",
                "location": "Online room",
                "reminder": "one_week",
                "event_color": "#8E6AC8",
                "event_type": "lecture",
            },
        )
        assert updated.status_code == 200
        edited = updated.json()
        assert edited["title"] == "Updated study group"
        assert edited["due_date"] == "2026-06-21T09:00:00+08:00"
        assert edited["start_time"] == "2026-06-21T09:00:00+08:00"
        assert edited["end_time"] == "2026-06-21T11:00:00+08:00"
        assert edited["description"] == "Review chapters 6 and 7"
        assert edited["notes"] == "Review chapters 6 and 7"
        assert edited["repeat"] == "monthly"
        assert edited["event_type"] == "lecture"

        cleared = client.patch(
            f"/api/events/{event['id']}",
            headers=headers,
            json={"description": "", "location": ""},
        )
        assert cleared.status_code == 200
        assert cleared.json()["description"] is None
        assert cleared.json()["notes"] is None
        assert cleared.json()["location"] is None

        invalid_update = client.patch(
            f"/api/events/{event['id']}",
            headers=headers,
            json={
                "start_time": "2026-06-21T12:00:00+08:00",
                "end_time": "2026-06-21T10:00:00+08:00",
            },
        )
        assert invalid_update.status_code == 422

        invalid = client.post(
            "/api/events",
            headers=headers,
            json={
                "title": "Invalid range",
                "start_time": "2026-06-20T15:00:00+08:00",
                "end_time": "2026-06-20T14:00:00+08:00",
            },
        )
        assert invalid.status_code == 422


def test_manual_preview_no_event_and_user_isolation() -> None:
    client, _ = make_client()
    with client:
        first_token = register(client, "one@example.edu")["token"]
        second_token = register(client, "two@example.edu")["token"]
        preview = client.post(
            "/api/extract-manual",
            headers={"Authorization": f"Bearer {first_token}"},
            json={
                "raw_content": "Assignment is due tomorrow.",
                "timestamp": "2026-06-11T10:00:00+08:00",
                "timezone": "Asia/Kuala_Lumpur",
                "save": False,
            },
        )
        assert preview.json()["status"] == "preview"
        assert client.get(
            "/api/events", headers={"Authorization": f"Bearer {first_token}"}
        ).json()["events"] == []
        no_event = client.post(
            "/api/extract-manual",
            headers={"Authorization": f"Bearer {second_token}"},
            json={
                "raw_content": "NO_EVENT general newsletter",
                "timestamp": "2026-06-11T10:00:00+08:00",
                "timezone": "Asia/Kuala_Lumpur",
            },
        )
        assert no_event.json()["status"] == "no_events_found"


def test_extracted_date_without_time_is_saved_as_all_day() -> None:
    client, storage = make_client()
    with client:
        token = register(client)["token"]
        headers = {"Authorization": f"Bearer {token}"}
        response = client.post(
            "/api/extract-manual",
            headers=headers,
            json={
                "raw_content": "ALL_DAY Assignment is due on 18 June.",
                "timestamp": "2026-06-11T10:00:00+08:00",
                "timezone": "Asia/Kuala_Lumpur",
                "save": True,
            },
        )
        assert response.status_code == 200
        event = response.json()["events"][0]
        assert event["all_day"] is True
        stored = next(iter(storage.events.values()))
        assert stored["start_time"] == "2026-06-18T00:00:00+08:00"
        assert stored["end_time"] == "2026-06-19T00:00:00+08:00"


def test_email_preview_requires_explicit_confirmation_before_storage() -> None:
    client, storage = make_client()
    with client:
        token = register(client)["token"]
        headers = {"Authorization": f"Bearer {token}"}
        payload = {
            "metadata": {
                "sender": "lecturer@university.edu",
                "subject": "Database Assignment Submission",
                "timestamp": "2026-06-11T10:00:00+08:00",
                "timezone": "Asia/Kuala_Lumpur",
            },
            "raw_content": "Submit by Friday at 11:59 PM through the LMS.",
            "email_hash": "sha256:" + ("e" * 64),
        }
        preview = client.post("/api/preview-email", headers=headers, json=payload)
        assert preview.status_code == 200
        assert preview.json()["status"] == "preview"
        assert storage.events == {}
        assert storage.hashes == set()

        confirmation = client.post(
            "/api/confirm-extraction",
            headers=headers,
            json={
                "metadata": payload["metadata"],
                "email_hash": payload["email_hash"],
                "events": preview.json()["events"],
            },
        )
        assert confirmation.status_code == 200
        assert confirmation.json()["status"] == "success"
        assert len(storage.events) == 1
        duplicate = client.post(
            "/api/confirm-extraction",
            headers=headers,
            json={
                "metadata": payload["metadata"],
                "email_hash": payload["email_hash"],
                "events": preview.json()["events"],
            },
        )
        assert duplicate.json()["status"] == "already_processed"


def test_manual_review_confirmation_preserves_ai_evidence_and_deduplicates() -> None:
    client, storage = make_client()
    with client:
        token = register(client)["token"]
        headers = {"Authorization": f"Bearer {token}"}
        preview_payload = {
            "raw_content": "Assignment is due Friday at 11:59 PM.",
            "subject": "Assignment reminder",
            "sender": "lecturer@university.edu",
            "timestamp": "2026-06-11T10:00:00+08:00",
            "timezone": "Asia/Kuala_Lumpur",
            "save": False,
        }
        preview = client.post(
            "/api/extract-manual", headers=headers, json=preview_payload
        ).json()
        reviewed_event = {
            **preview["events"][0],
            "title": "Reviewed Database Assignment",
            "confidence": "low",
            "evidence": "Assignment is due Friday at 11:59 PM.",
            "source_email_subject": "Assignment reminder",
            "source_email_sender": "lecturer@university.edu",
        }
        confirmation_payload = {
            key: value for key, value in preview_payload.items() if key != "save"
        }
        confirmation_payload["events"] = [reviewed_event]
        saved = client.post(
            "/api/confirm-manual-extraction",
            headers=headers,
            json=confirmation_payload,
        )
        assert saved.status_code == 200
        assert saved.json()["status"] == "success"
        row = next(iter(storage.events.values()))
        assert row["title"] == "Reviewed Database Assignment"
        assert row["confidence"] == "low"
        assert row["evidence"].startswith("Assignment is due")

        confirmation_payload["timestamp"] = "2026-06-11T11:00:00+08:00"
        duplicate = client.post(
            "/api/confirm-manual-extraction",
            headers=headers,
            json=confirmation_payload,
        )
        assert duplicate.json()["status"] == "already_processed"


def test_arabic_and_english_samples_filters_and_ownership() -> None:
    client, _ = make_client()
    with client:
        first_token = register(client, "first@example.edu")["token"]
        second_token = register(client, "second@example.edu")["token"]
        first_headers = {"Authorization": f"Bearer {first_token}"}
        second_headers = {"Authorization": f"Bearer {second_token}"}

        samples = [
            (
                "sha256:" + ("b" * 64),
                "Database Assignment Submission",
                "Submit by Friday at 11:59 PM through the LMS.",
                "deadline",
            ),
            (
                "sha256:" + ("c" * 64),
                "اختبار منتصف الفصل - رياضيات متقدمة",
                "سيكون اختبار منتصف الفصل الثلاثاء الساعة 1:00 في القاعة 4-B.",
                "exam",
            ),
        ]
        for email_hash, subject, body, expected_type in samples:
            response = client.post(
                "/api/process-email",
                headers=first_headers,
                json={
                    "metadata": {
                        "sender": "lecturer@university.edu",
                        "subject": subject,
                        "timestamp": "2026-06-11T10:00:00+08:00",
                        "timezone": "Asia/Kuala_Lumpur",
                    },
                    "raw_content": body,
                    "email_hash": email_hash,
                },
            )
            assert response.status_code == 200
            assert response.json()["events"][0]["event_type"] == expected_type

        exams = client.get(
            "/api/events",
            headers=first_headers,
            params={
                "from_date": "2026-06-15T00:00:00+08:00",
                "to_date": "2026-06-17T23:59:59+08:00",
                "type": "exam",
            },
        ).json()["events"]
        assert len(exams) == 1
        assert exams[0]["title"] == "اختبار منتصف الفصل"

        event_id = exams[0]["id"]
        assert client.delete(
            f"/api/events/{event_id}", headers=second_headers
        ).status_code == 404
        assert client.patch(
            f"/api/events/{event_id}",
            headers=second_headers,
            json={"title": "Not yours"},
        ).status_code == 404
        assert len(client.get("/api/events", headers=first_headers).json()["events"]) == 2


def test_token_expiration() -> None:
    client, _ = make_client()
    with client:
        auth = register(client, "expire@example.edu")
        user_id = auth["user"]["id"]
        from app.core.security import create_access_token
        expired_token = create_access_token(
            user_id,
            client.app.state.settings.jwt_secret,
            expires_delta=timedelta(days=-1)
        )
        r = client.get("/api/settings", headers={"Authorization": f"Bearer {expired_token}"})
        assert r.status_code == 401
        assert "Invalid authentication token" in r.json()["detail"]


def test_rate_limiting() -> None:
    client, _ = make_client()
    with client:
        payload = {"email": "rate@example.edu", "password": "wrong"}
        status_codes = []
        for _ in range(6):
            r = client.post("/api/auth/login", json=payload)
            status_codes.append(r.status_code)
        assert status_codes[:5] == [401, 401, 401, 401, 401]
        assert status_codes[5] == 429
        assert "Too many requests" in r.json()["detail"]


def test_missing_openrouter_key_returns_clear_service_error() -> None:
    reset_rate_limiter()
    storage = InMemoryStorage()
    settings = Settings(
        environment="test",
        database_backend="memory",
        jwt_secret="test-secret-that-is-long-enough",
    )
    client = TestClient(create_app(settings, storage))
    with client:
        token = register(client)["token"]
        response = client.post(
            "/api/preview-email",
            headers={"Authorization": f"Bearer {token}"},
            json={
                "metadata": {
                    "sender": "lecturer@university.edu",
                    "subject": "Exam",
                    "timestamp": "2026-06-11T10:00:00+08:00",
                    "timezone": "Asia/Kuala_Lumpur",
                },
                "raw_content": "Exam on 20 June at 9 AM.",
                "email_hash": "sha256:" + ("f" * 64),
            },
        )
        assert response.status_code == 503
        assert "OPENROUTER_API_KEY" in response.json()["detail"]
