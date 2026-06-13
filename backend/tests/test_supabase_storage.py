from datetime import datetime

from app.core.config import Settings
from app.schemas.event import ExtractedEvent
from app.services.storage import SupabaseStorage


class FakeResponse:
    def __init__(self, body: list[dict]) -> None:
        self.body = body

    def raise_for_status(self) -> None:
        return None

    def json(self) -> list[dict]:
        return self.body


class FakeClient:
    def __init__(self) -> None:
        self.calls: list[tuple[str, str, dict]] = []

    def request(self, method: str, url: str, **kwargs) -> FakeResponse:
        self.calls.append((method, url, kwargs))
        return FakeResponse([{"id": "event-id"}])


def test_save_extraction_uses_single_atomic_rpc() -> None:
    storage = SupabaseStorage(
        Settings(
            environment="test",
            database_backend="supabase",
            jwt_secret="test-secret-that-is-long-enough",
            supabase_url="https://example.supabase.co",
            supabase_service_key="test-service-key",
        )
    )
    fake_client = FakeClient()
    storage.client = fake_client  # type: ignore[assignment]
    event = ExtractedEvent(
        title="Database Assignment",
        course_code="DBS201",
        event_type="deadline",
        due_date=datetime.fromisoformat("2026-06-12T23:59:00+08:00"),
        location="LMS / Online",
        notes=None,
    )

    rows = storage.save_extraction(
        "user-id",
        "sha256:" + ("d" * 64),
        "Assignment",
        [event],
    )

    assert rows == [{"id": "event-id"}]
    assert len(fake_client.calls) == 1
    method, url, kwargs = fake_client.calls[0]
    assert method == "POST"
    assert url.endswith("/rpc/save_extraction")
    assert kwargs["json"]["p_events"][0]["title"] == "Database Assignment"
