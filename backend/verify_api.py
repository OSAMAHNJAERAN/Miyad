import httpx

BASE_URL = "http://127.0.0.1:8000"

def test_flow():
    print("=== Testing Health ===")
    r = httpx.get(f"{BASE_URL}/health")
    print("Health Status Code:", r.status_code)
    print("Health Response:", r.json())
    assert r.status_code == 200

    print("\n=== Testing Registration ===")
    reg_payload = {
        "email": "test_verify@example.edu",
        "password": "secure-password-here",
        "name": "Audit Verification",
        "university": "Audit University"
    }
    r = httpx.post(f"{BASE_URL}/api/auth/register", json=reg_payload)
    print("Register Status Code:", r.status_code)
    print("Register Response:", r.json())
    assert r.status_code in (201, 400, 409) # 400/409 if already exists

    print("\n=== Testing Login ===")
    login_payload = {
        "email": "test_verify@example.edu",
        "password": "secure-password-here"
    }
    r = httpx.post(f"{BASE_URL}/api/auth/login", json=login_payload)
    print("Login Status Code:", r.status_code)
    login_data = r.json()
    print("Login Response keys:", list(login_data.keys()))
    assert r.status_code == 200
    token = login_data["token"]
    headers = {"Authorization": f"Bearer {token}"}

    print("\n=== Testing Invalid Login ===")
    r = httpx.post(f"{BASE_URL}/api/auth/login", json={
        "email": "test_verify@example.edu",
        "password": "wrong-password"
    })
    print("Invalid Login Status Code:", r.status_code)
    assert r.status_code == 401

    print("\n=== Testing Protected Route Settings (Default) ===")
    r = httpx.get(f"{BASE_URL}/api/settings", headers=headers)
    print("Settings Status Code:", r.status_code)
    print("Settings Response:", r.json())
    assert r.status_code == 200

    print("\n=== Testing Extension Heartbeat ===")
    r = httpx.post(f"{BASE_URL}/api/extension/heartbeat", headers=headers)
    print("Heartbeat Status Code:", r.status_code)
    print("Heartbeat Response:", r.json())
    assert r.status_code == 200

    print("\n=== Testing Protected Route Settings (After Heartbeat) ===")
    r = httpx.get(f"{BASE_URL}/api/settings", headers=headers)
    print("Settings Response after Heartbeat:", r.json())
    assert r.json()["extension_connected"] is True

    print("\n=== Testing Event Creation ===")
    event_payload = {
        "title": "Study Group Session",
        "description": "Prepare for the final exam",
        "start_time": "2026-06-20T14:00:00+08:00",
        "end_time": "2026-06-20T15:30:00+08:00",
        "all_day": False,
        "repeat": "weekly",
        "location": "Virtual",
        "reminder": "same_day",
        "event_color": "#FF5733",
        "event_type": "other"
    }
    r = httpx.post(f"{BASE_URL}/api/events", headers=headers, json=event_payload)
    print("Create Event Status Code:", r.status_code)
    print("Create Event Response:", r.json())
    assert r.status_code == 201
    event_id = r.json()["id"]

    print("\n=== Testing Invalid Event Creation (End before Start) ===")
    invalid_event = event_payload.copy()
    invalid_event["end_time"] = "2026-06-20T13:00:00+08:00"
    r = httpx.post(f"{BASE_URL}/api/events", headers=headers, json=invalid_event)
    print("Invalid Event Status Code:", r.status_code)
    print("Invalid Event Response:", r.json())
    assert r.status_code == 422

    print("\n=== Testing Event Listing & Filtering ===")
    r = httpx.get(f"{BASE_URL}/api/events", headers=headers, params={"type": "other"})
    print("List Events Status Code:", r.status_code)
    print("List Events Count:", len(r.json()["events"]))
    assert r.status_code == 200

    print("\n=== Testing Event Patch/Update ===")
    r = httpx.patch(f"{BASE_URL}/api/events/{event_id}", headers=headers, json={"location": "Library Room 1"})
    print("Patch Event Status Code:", r.status_code)
    print("Patch Event Response:", r.json())
    assert r.status_code == 200

    print("\n=== Testing Process Email ===")
    email_payload = {
        "metadata": {
            "sender": "prof@university.edu",
            "subject": "CS101 Final Exam Details",
            "timestamp": "2026-06-15T09:00:00+08:00",
            "timezone": "Asia/Kuala_Lumpur"
        },
        "raw_content": "The CS101 final exam will be on June 20th at 10 AM in Hall C.",
        "email_hash": "sha256:" + ("a" * 64)
    }
    r = httpx.post(f"{BASE_URL}/api/process-email", headers=headers, json=email_payload)
    print("Process Email Status Code:", r.status_code)
    print("Process Email Response:", r.json())
    assert r.status_code == 200

    print("\n=== Testing Duplicate Email Process ===")
    r = httpx.post(f"{BASE_URL}/api/process-email", headers=headers, json=email_payload)
    print("Duplicate Email Status Code:", r.status_code)
    print("Duplicate Email Response:", r.json())
    assert r.json()["status"] == "already_processed"

    print("\n=== Testing Manual Extraction Preview ===")
    manual_payload = {
        "raw_content": "Math test next Monday at 9 AM.",
        "timestamp": "2026-06-15T09:00:00+08:00",
        "timezone": "Asia/Kuala_Lumpur",
        "save": False
    }
    r = httpx.post(f"{BASE_URL}/api/extract-manual", headers=headers, json=manual_payload)
    print("Manual Preview Status Code:", r.status_code)
    print("Manual Preview Response:", r.json())
    assert r.json()["status"] == "preview"

    print("\n=== Testing Manual Extraction Save ===")
    manual_payload["save"] = True
    r = httpx.post(f"{BASE_URL}/api/extract-manual", headers=headers, json=manual_payload)
    print("Manual Save Status Code:", r.status_code)
    print("Manual Save Response:", r.json())
    assert r.json()["status"] == "success"

    print("\n=== Testing Event Deletion ===")
    r = httpx.delete(f"{BASE_URL}/api/events/{event_id}", headers=headers)
    print("Delete Event Status Code:", r.status_code)
    print("Delete Event Response:", r.json())
    assert r.status_code == 200

    print("\n=== Testing Multi-Tenant Separation ===")
    # Register second user
    r = httpx.post(f"{BASE_URL}/api/auth/register", json={
        "email": "test_verify_two@example.edu",
        "password": "secure-password-here",
        "name": "Audit Verification Two",
        "university": "Audit University"
    })
    token_two = httpx.post(f"{BASE_URL}/api/auth/login", json={
        "email": "test_verify_two@example.edu",
        "password": "secure-password-here"
    }).json()["token"]
    headers_two = {"Authorization": f"Bearer {token_two}"}

    # First user creates event
    created = httpx.post(f"{BASE_URL}/api/events", headers=headers, json=event_payload).json()
    e_id = created["id"]

    # Second user tries to delete first user's event
    r = httpx.delete(f"{BASE_URL}/api/events/{e_id}", headers=headers_two)
    print("Second user deleting first user's event status:", r.status_code)
    assert r.status_code == 404

    # Clean up first user's event
    httpx.delete(f"{BASE_URL}/api/events/{e_id}", headers=headers)

    print("\nALL LOCAL API FLOW TESTS PASSED SUCCESSFULLY!")

if __name__ == "__main__":
    test_flow()
