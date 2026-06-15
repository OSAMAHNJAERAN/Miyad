import pytest
from datetime import datetime, timezone
from fastapi.testclient import TestClient

from app.services.storage import Storage


def test_courses_crud(client: TestClient, auth_headers: dict):
    # 1. List courses (should be empty initially)
    r = client.get("/api/courses", headers=auth_headers)
    assert r.status_code == 200
    assert r.json() == []

    # 2. Add a course
    payload = {
        "course_code": "CS101",
        "course_name": "Intro to Computer Science",
        "teaching_plan": "Midterm Exam on Week 8. Project submission on Week 12."
    }
    r = client.post("/api/courses", headers=auth_headers, json=payload)
    assert r.status_code == 201
    data = r.json()
    assert data["course_code"] == "CS101"
    assert data["course_name"] == "Intro to Computer Science"
    assert data["teaching_plan"] == "Midterm Exam on Week 8. Project submission on Week 12."
    assert "id" in data

    # 3. List courses again
    r = client.get("/api/courses", headers=auth_headers)
    assert r.status_code == 200
    assert len(r.json()) == 1
    assert r.json()[0]["course_code"] == "CS101"

    # 4. Delete course
    r = client.delete("/api/courses/CS101", headers=auth_headers)
    assert r.status_code == 200
    assert r.json() == {"success": True}

    # 5. List courses (should be empty)
    r = client.get("/api/courses", headers=auth_headers)
    assert len(r.json()) == 0


def test_schedule_crud(client: TestClient, auth_headers: dict):
    # Setup: add CS101 course first (required by business logic validation)
    client.post("/api/courses", headers=auth_headers, json={
        "course_code": "CS101",
        "course_name": "Intro to Computer Science"
    })

    # 1. List schedule (should be empty)
    r = client.get("/api/schedule", headers=auth_headers)
    assert r.status_code == 200
    assert r.json() == []

    # 2. Add slot
    payload = {
        "course_code": "CS101",
        "day_of_week": "Monday",
        "start_time": "09:00:00",
        "end_time": "10:30:00",
        "location": "Lab 2"
    }
    r = client.post("/api/schedule", headers=auth_headers, json=payload)
    assert r.status_code == 201
    data = r.json()
    assert data["course_code"] == "CS101"
    assert data["day_of_week"] == "Monday"
    assert data["start_time"] == "09:00:00"
    assert data["end_time"] == "10:30:00"
    assert data["location"] == "Lab 2"
    assert "id" in data
    slot_id = data["id"]

    # 3. List schedule
    r = client.get("/api/schedule", headers=auth_headers)
    assert len(r.json()) == 1

    # 4. Delete slot
    r = client.delete(f"/api/schedule/{slot_id}", headers=auth_headers)
    assert r.status_code == 200

    # 5. List schedule
    r = client.get("/api/schedule", headers=auth_headers)
    assert len(r.json()) == 0


def test_smart_verification_flow(client: TestClient, auth_headers: dict):
    # Reset courses & schedule for user
    # Add CS101 with weekly schedule on Monday 9 AM and a teaching plan
    client.post("/api/courses", headers=auth_headers, json={
        "course_code": "CS101",
        "course_name": "Intro to Computer Science",
        "teaching_plan": "midterm exam on 2026-06-22"
    })
    client.post("/api/schedule", headers=auth_headers, json={
        "course_code": "CS101",
        "day_of_week": "Monday",
        "start_time": "09:00:00",
        "end_time": "10:30:00"
    })

    # Test Case 1: Match course code and schedule day (2026-06-22 is a Monday)
    email_payload = {
        "metadata": {
            "sender": "prof@uni.edu",
            "subject": "CS101 Class details",
            "timestamp": "2026-06-15T09:00:00+08:00",
            "timezone": "Asia/Kuala_Lumpur"
        },
        "raw_content": "Dear students, the CS101 class will meet in Hall B on Monday June 22nd at 9:00 AM.",
        "email_hash": "sha256:" + ("b" * 64)
    }
    r = client.post("/api/process-email", headers=auth_headers, json=email_payload)
    assert r.status_code == 200
    # Fallback extractor matches MATH301 or CS101 based on keywords. "CS101" is in raw_content, so it will extract CS101 event.
    # CS101 is scheduled on Monday. 2026-06-16 is Tuesday, but wait, the mock extractor due date is 2026-06-16 or 2026-06-12.
    # Since mock extraction returns 2026-06-16T13:00:00+08:00 (Tuesday) or similar:
    # Let's verify how it handles alert creation!
    
    # Let's check pending alerts
    r = client.get("/api/alerts", headers=auth_headers)
    assert r.status_code == 200
    alerts = r.json()
    assert len(alerts) > 0
    alert = alerts[0]
    assert alert["status"] == "pending"
    alert_id = alert["id"]

    # Resolve alert: confirm (add to calendar)
    r = client.post(f"/api/alerts/{alert_id}/resolve", headers=auth_headers, json={"action": "confirm"})
    assert r.status_code == 200
    assert r.json()["status"] == "confirmed"

    # Verify event was added to calendar
    r = client.get("/api/events", headers=auth_headers)
    assert r.status_code == 200
    events = r.json()["events"]
    assert len(events) > 0
    assert any(e["title"] == alert["event_data"]["title"] for e in events)


def test_smart_verification_flow_reject(client: TestClient, auth_headers: dict):
    # Setup: Enroll in at least one course so that non-matching courses trigger alert
    client.post("/api/courses", headers=auth_headers, json={
        "course_code": "CS101",
        "course_name": "Intro to Computer Science"
    })

    # Verify rejection flow
    email_payload = {
        "metadata": {
            "sender": "prof@uni.edu",
            "subject": "MATH301 Homework Alert",
            "timestamp": "2026-06-15T09:00:00+08:00",
            "timezone": "Asia/Kuala_Lumpur"
        },
        "raw_content": "Submit MATH301 homework by next Monday.",
        "email_hash": "sha256:" + ("c" * 64)
    }
    r = client.post("/api/process-email", headers=auth_headers, json=email_payload)
    assert r.status_code == 200

    r = client.get("/api/alerts", headers=auth_headers)
    pending_alerts = [a for a in r.json() if a["status"] == "pending"]
    assert len(pending_alerts) > 0
    alert = pending_alerts[0]

    # Resolve alert: reject
    r = client.post(f"/api/alerts/{alert['id']}/resolve", headers=auth_headers, json={"action": "reject"})
    assert r.status_code == 200
    assert r.json()["status"] == "rejected"
