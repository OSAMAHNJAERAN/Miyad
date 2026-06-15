import pytest
from datetime import datetime
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
        
        is_arabic = "اختبار" in raw_content or "MATH301" in raw_content
        is_cs101 = "CS101" in raw_content
        is_all_day = "ALL_DAY" in raw_content
        
        title = "Database Assignment"
        course_code = "DBS201"
        due_date = "2026-06-12T23:59:00+08:00"
        
        if is_arabic:
            title = "اختبار منتصف الفصل"
            course_code = "MATH301"
            due_date = "2026-06-16T13:00:00+08:00"
        elif is_cs101:
            title = "CS101 Midterm Exam"
            course_code = "CS101"
            due_date = "2026-06-16T13:00:00+08:00"  # Tuesday, mismatches Monday weekly schedule
            
        return [
            ExtractedEvent(
                title=title,
                course_code=course_code,
                event_type="exam" if (is_arabic or is_cs101) else "deadline",
                due_date=datetime.fromisoformat(
                    "2026-06-18T00:00:00+08:00"
                    if is_all_day
                    else due_date
                ),
                all_day=is_all_day,
                location="القاعة 4-B" if (is_arabic or is_cs101) else "LMS / Online",
                notes="Some notes",
            )
        ]


@pytest.fixture
def app_client_and_storage():
    reset_rate_limiter()
    storage = InMemoryStorage()
    settings = Settings(
        environment="test",
        database_backend="memory",
        jwt_secret="test-secret-that-is-long-enough",
    )
    app = create_app(settings, storage, FakeExtractor())
    return TestClient(app), storage


@pytest.fixture
def client(app_client_and_storage):
    client_obj, _ = app_client_and_storage
    with client_obj as c:
        yield c


@pytest.fixture
def auth_headers(client):
    response = client.post(
        "/api/auth/register",
        json={
            "email": "student@example.edu",
            "password": "strong-password",
            "name": "Student",
            "university": "Example University",
        },
    )
    assert response.status_code == 201
    token = response.json()["token"]
    return {"Authorization": f"Bearer {token}"}
