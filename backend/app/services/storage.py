from __future__ import annotations

from abc import ABC, abstractmethod
from datetime import datetime, timedelta, timezone
from threading import RLock
from typing import Any
from uuid import uuid4

import httpx

from app.core.config import Settings
from app.schemas.event import EventCreate, EventUpdate, ExtractedEvent
from app.schemas.settings import UserSettings, UserSettingsUpdate


class Storage(ABC):
    @abstractmethod
    def create_user(self, data: dict[str, Any]) -> dict[str, Any]: ...

    @abstractmethod
    def get_user_by_email(self, email: str) -> dict[str, Any] | None: ...

    @abstractmethod
    def get_user(self, user_id: str) -> dict[str, Any] | None: ...

    @abstractmethod
    def hash_exists(self, user_id: str, email_hash: str) -> bool: ...

    @abstractmethod
    def save_extraction(
        self,
        user_id: str,
        email_hash: str,
        subject: str,
        events: list[ExtractedEvent],
    ) -> list[dict[str, Any]]: ...

    @abstractmethod
    def list_events(
        self,
        user_id: str,
        from_date: datetime | None,
        to_date: datetime | None,
        event_type: str | None,
    ) -> list[dict[str, Any]]: ...

    @abstractmethod
    def create_event(
        self, user_id: str, event: EventCreate
    ) -> dict[str, Any]: ...

    @abstractmethod
    def delete_event(self, user_id: str, event_id: str) -> bool: ...

    @abstractmethod
    def update_event(
        self, user_id: str, event_id: str, update: EventUpdate
    ) -> dict[str, Any] | None: ...

    @abstractmethod
    def get_settings(self, user_id: str) -> dict[str, Any]: ...

    @abstractmethod
    def update_settings(
        self, user_id: str, update: UserSettingsUpdate
    ) -> dict[str, Any]: ...

    @abstractmethod
    def mark_extension_seen(self, user_id: str) -> None: ...

    @abstractmethod
    def list_courses(self, user_id: str) -> list[dict[str, Any]]: ...

    @abstractmethod
    def create_or_update_course(self, user_id: str, course_code: str, course_name: str, teaching_plan: str | None) -> dict[str, Any]: ...

    @abstractmethod
    def delete_course(self, user_id: str, course_code: str) -> bool: ...

    @abstractmethod
    def list_schedule(self, user_id: str) -> list[dict[str, Any]]: ...

    @abstractmethod
    def create_schedule(self, user_id: str, data: dict[str, Any]) -> dict[str, Any]: ...

    @abstractmethod
    def delete_schedule(self, user_id: str, slot_id: str) -> bool: ...

    @abstractmethod
    def list_alerts(self, user_id: str, status: str | None = None) -> list[dict[str, Any]]: ...

    @abstractmethod
    def get_alert(self, user_id: str, alert_id: str) -> dict[str, Any] | None: ...

    @abstractmethod
    def create_alert(self, user_id: str, alert_data: dict[str, Any]) -> dict[str, Any]: ...

    @abstractmethod
    def update_alert_status(self, user_id: str, alert_id: str, status: str) -> dict[str, Any] | None: ...


class InMemoryStorage(Storage):
    def __init__(self) -> None:
        self.users: dict[str, dict[str, Any]] = {}
        self.user_ids_by_email: dict[str, str] = {}
        self.courses: dict[str, dict[str, Any]] = {}
        self.study_schedule: dict[str, dict[str, Any]] = {}
        self.verification_alerts: dict[str, dict[str, Any]] = {}
        self.hashes: set[tuple[str, str]] = set()
        self.events: dict[str, dict[str, Any]] = {}
        self.settings: dict[str, dict[str, Any]] = {}
        self.extension_last_seen: dict[str, datetime] = {}
        self._lock = RLock()

    def create_user(self, data: dict[str, Any]) -> dict[str, Any]:
        email = data["email"].lower()
        with self._lock:
            if email in self.user_ids_by_email:
                raise ValueError("email_exists")
            user_id = str(uuid4())
            user = {
                "id": user_id,
                **data,
                "email": email,
                "preferred_language": data.get("preferred_language", "ar"),
                "created_at": datetime.now(timezone.utc).isoformat(),
            }
            self.users[user_id] = user
            self.user_ids_by_email[email] = user_id
            self.settings[user_id] = UserSettings().model_dump(
                exclude={"extension_connected"}
            )
            return dict(user)

    def get_user_by_email(self, email: str) -> dict[str, Any] | None:
        user_id = self.user_ids_by_email.get(email.lower())
        return dict(self.users[user_id]) if user_id else None

    def get_user(self, user_id: str) -> dict[str, Any] | None:
        user = self.users.get(user_id)
        return dict(user) if user else None

    def hash_exists(self, user_id: str, email_hash: str) -> bool:
        return (user_id, email_hash) in self.hashes

    def save_extraction(
        self,
        user_id: str,
        email_hash: str,
        subject: str,
        events: list[ExtractedEvent],
    ) -> list[dict[str, Any]]:
        with self._lock:
            if (user_id, email_hash) in self.hashes:
                return []
            now = datetime.now(timezone.utc)
            rows = []
            for item in events:
                row = {
                    "id": str(uuid4()),
                    "user_id": user_id,
                    **item.model_dump(mode="json"),
                    "description": item.notes,
                    "start_time": item.due_date.isoformat(),
                    "end_time": (
                        item.due_date
                        + (timedelta(days=1) if item.all_day else timedelta(hours=1))
                    ).isoformat(),
                    "all_day": item.all_day,
                    "repeat": "none",
                    "event_color": "#BCDA4B",
                    "source_hash": email_hash,
                    "created_at": now.isoformat(),
                    "reminder": "one_day",
                }
                self.events[row["id"]] = row
                rows.append(dict(row))
            self.hashes.add((user_id, email_hash))
            return rows

    def list_events(
        self,
        user_id: str,
        from_date: datetime | None,
        to_date: datetime | None,
        event_type: str | None,
    ) -> list[dict[str, Any]]:
        rows = []
        for row in self.events.values():
            if row["user_id"] != user_id:
                continue
            due_date = datetime.fromisoformat(row["due_date"])
            if from_date and due_date < from_date:
                continue
            if to_date and due_date > to_date:
                continue
            if event_type and row["event_type"] != event_type:
                continue
            rows.append(dict(row))
        return sorted(rows, key=lambda item: item["due_date"])

    def create_event(self, user_id: str, event: EventCreate) -> dict[str, Any]:
        now = datetime.now(timezone.utc)
        row = {
            "id": str(uuid4()),
            "user_id": user_id,
            "title": event.title,
            "course_code": None,
            "event_type": event.event_type,
            "due_date": event.start_time.isoformat(),
            "location": event.location,
            "notes": event.description,
            "description": event.description,
            "start_time": event.start_time.isoformat(),
            "end_time": event.end_time.isoformat(),
            "all_day": event.all_day,
            "repeat": event.repeat,
            "event_color": event.event_color,
            "source_hash": None,
            "created_at": now.isoformat(),
            "reminder": event.reminder,
        }
        with self._lock:
            self.events[row["id"]] = row
        return dict(row)

    def delete_event(self, user_id: str, event_id: str) -> bool:
        row = self.events.get(event_id)
        if not row or row["user_id"] != user_id:
            return False
        del self.events[event_id]
        return True

    def update_event(
        self, user_id: str, event_id: str, update: EventUpdate
    ) -> dict[str, Any] | None:
        row = self.events.get(event_id)
        if not row or row["user_id"] != user_id:
            return None
        changes = update.model_dump(exclude_none=True, mode="json")
        for field in ("description", "notes", "location"):
            if changes.get(field) == "":
                changes[field] = None
        if "start_time" in changes and "due_date" not in changes:
            changes["due_date"] = changes["start_time"]
        if "due_date" in changes and "start_time" not in changes:
            changes["start_time"] = changes["due_date"]
        if "description" in changes and "notes" not in changes:
            changes["notes"] = changes["description"]
        if "notes" in changes and "description" not in changes:
            changes["description"] = changes["notes"]
        effective_start = datetime.fromisoformat(
            changes.get("start_time", row["start_time"])
        )
        effective_end = datetime.fromisoformat(
            changes.get("end_time", row["end_time"])
        )
        if effective_end <= effective_start:
            raise ValueError("end_time must be after start_time")
        row.update(changes)
        return dict(row)

    def get_settings(self, user_id: str) -> dict[str, Any]:
        current = dict(
            self.settings.setdefault(
                user_id,
                UserSettings().model_dump(exclude={"extension_connected"}),
            )
        )
        last_seen = self.extension_last_seen.get(user_id)
        current["extension_connected"] = bool(
            last_seen
            and last_seen >= datetime.now(timezone.utc) - timedelta(days=7)
        )
        return current

    def update_settings(
        self, user_id: str, update: UserSettingsUpdate
    ) -> dict[str, Any]:
        current = self.settings.setdefault(
            user_id,
            UserSettings().model_dump(exclude={"extension_connected"}),
        )
        current.update(update.model_dump(exclude_none=True))
        if "preferred_language" in update.model_fields_set:
            self.users[user_id]["preferred_language"] = current["preferred_language"]
        return self.get_settings(user_id)

    def mark_extension_seen(self, user_id: str) -> None:
        self.extension_last_seen[user_id] = datetime.now(timezone.utc)

    def list_courses(self, user_id: str) -> list[dict[str, Any]]:
        with self._lock:
            return [dict(c) for c in self.courses.values() if c["user_id"] == user_id]

    def create_or_update_course(self, user_id: str, course_code: str, course_name: str, teaching_plan: str | None) -> dict[str, Any]:
        with self._lock:
            for c in self.courses.values():
                if c["user_id"] == user_id and c["course_code"] == course_code:
                    c.update({
                        "course_name": course_name,
                        "teaching_plan": teaching_plan,
                    })
                    return dict(c)
            cid = str(uuid4())
            course = {
                "id": cid,
                "user_id": user_id,
                "course_code": course_code,
                "course_name": course_name,
                "teaching_plan": teaching_plan,
                "created_at": datetime.now(timezone.utc)
            }
            self.courses[cid] = course
            return dict(course)

    def delete_course(self, user_id: str, course_code: str) -> bool:
        with self._lock:
            target_id = None
            for cid, c in list(self.courses.items()):
                if c["user_id"] == user_id and c["course_code"] == course_code:
                    target_id = cid
                    break
            if not target_id:
                return False
            del self.courses[target_id]
            for sid, s in list(self.study_schedule.items()):
                if s["user_id"] == user_id and s["course_code"] == course_code:
                    del self.study_schedule[sid]
            return True

    def list_schedule(self, user_id: str) -> list[dict[str, Any]]:
        with self._lock:
            return [dict(s) for s in self.study_schedule.values() if s["user_id"] == user_id]

    def create_schedule(self, user_id: str, data: dict[str, Any]) -> dict[str, Any]:
        with self._lock:
            sid = str(uuid4())
            start_time = data["start_time"]
            if hasattr(start_time, "isoformat"):
                start_time = start_time.isoformat()
            end_time = data["end_time"]
            if hasattr(end_time, "isoformat"):
                end_time = end_time.isoformat()
            slot = {
                "id": sid,
                "user_id": user_id,
                "course_code": data["course_code"],
                "day_of_week": data["day_of_week"],
                "start_time": start_time,
                "end_time": end_time,
                "location": data.get("location"),
                "created_at": datetime.now(timezone.utc)
            }
            self.study_schedule[sid] = slot
            return dict(slot)

    def delete_schedule(self, user_id: str, slot_id: str) -> bool:
        with self._lock:
            s = self.study_schedule.get(slot_id)
            if not s or s["user_id"] != user_id:
                return False
            del self.study_schedule[slot_id]
            return True

    def list_alerts(self, user_id: str, status: str | None = None) -> list[dict[str, Any]]:
        with self._lock:
            results = []
            for a in self.verification_alerts.values():
                if a["user_id"] == user_id:
                    if status is None or a["status"] == status:
                        results.append(dict(a))
            return results

    def get_alert(self, user_id: str, alert_id: str) -> dict[str, Any] | None:
        with self._lock:
            a = self.verification_alerts.get(alert_id)
            if not a or a["user_id"] != user_id:
                return None
            return dict(a)

    def create_alert(self, user_id: str, alert_data: dict[str, Any]) -> dict[str, Any]:
        with self._lock:
            aid = str(uuid4())
            alert = {
                "id": aid,
                "user_id": user_id,
                "event_data": alert_data["event_data"],
                "email_hash": alert_data["email_hash"],
                "alert_type": alert_data["alert_type"],
                "ai_reason": alert_data.get("ai_reason"),
                "confidence": alert_data.get("confidence", "low"),
                "status": "pending",
                "created_at": datetime.now(timezone.utc),
                "resolved_at": None
            }
            self.verification_alerts[aid] = alert
            return dict(alert)

    def update_alert_status(self, user_id: str, alert_id: str, status: str) -> dict[str, Any] | None:
        with self._lock:
            a = self.verification_alerts.get(alert_id)
            if not a or a["user_id"] != user_id:
                return None
            a["status"] = status
            a["resolved_at"] = datetime.now(timezone.utc)
            return dict(a)


class SupabaseStorage(Storage):
    def __init__(self, settings: Settings) -> None:
        if not settings.supabase_url or not settings.supabase_service_key:
            raise RuntimeError("Supabase configuration is required")
        self.base_url = f"{settings.supabase_url.rstrip('/')}/rest/v1"
        self.client = httpx.Client(
            timeout=20,
            headers={
                "apikey": settings.supabase_service_key,
                "Authorization": f"Bearer {settings.supabase_service_key}",
                "Content-Type": "application/json",
            },
        )

    def _request(self, method: str, path: str, **kwargs: Any) -> httpx.Response:
        response = self.client.request(method, f"{self.base_url}/{path}", **kwargs)
        response.raise_for_status()
        return response

    def create_user(self, data: dict[str, Any]) -> dict[str, Any]:
        response = self._request(
            "POST", "users", json=data, headers={"Prefer": "return=representation"}
        )
        return response.json()[0]

    def get_user_by_email(self, email: str) -> dict[str, Any] | None:
        rows = self._request(
            "GET", "users", params={"email": f"eq.{email.lower()}", "limit": "1"}
        ).json()
        return rows[0] if rows else None

    def get_user(self, user_id: str) -> dict[str, Any] | None:
        rows = self._request(
            "GET", "users", params={"id": f"eq.{user_id}", "limit": "1"}
        ).json()
        return rows[0] if rows else None

    def hash_exists(self, user_id: str, email_hash: str) -> bool:
        rows = self._request(
            "GET",
            "email_hashes",
            params={
                "user_id": f"eq.{user_id}",
                "hash": f"eq.{email_hash}",
                "select": "hash",
                "limit": "1",
            },
        ).json()
        return bool(rows)

    def save_extraction(
        self,
        user_id: str,
        email_hash: str,
        subject: str,
        events: list[ExtractedEvent],
    ) -> list[dict[str, Any]]:
        # The RPC inserts the hash and all events in one PostgreSQL transaction.
        return self._request(
            "POST",
            "rpc/save_extraction",
            json={
                "p_user_id": user_id,
                "p_hash": email_hash,
                "p_subject": subject,
                "p_events": [
                    event.model_dump(mode="json")
                    for event in events
                ],
            },
            headers={"Prefer": "return=representation"},
        ).json()

    def list_events(
        self,
        user_id: str,
        from_date: datetime | None,
        to_date: datetime | None,
        event_type: str | None,
    ) -> list[dict[str, Any]]:
        params = {"user_id": f"eq.{user_id}", "order": "due_date.asc"}
        date_filters = []
        if from_date:
            date_filters.append(f"due_date.gte.{from_date.isoformat()}")
        if to_date:
            date_filters.append(f"due_date.lte.{to_date.isoformat()}")
        if date_filters:
            params["and"] = f"({','.join(date_filters)})"
        if event_type:
            params["event_type"] = f"eq.{event_type}"
        return self._request("GET", "events", params=params).json()

    def create_event(self, user_id: str, event: EventCreate) -> dict[str, Any]:
        payload = {
            "user_id": user_id,
            "title": event.title,
            "event_type": event.event_type,
            "due_date": event.start_time.isoformat(),
            "location": event.location,
            "notes": event.description,
            "description": event.description,
            "start_time": event.start_time.isoformat(),
            "end_time": event.end_time.isoformat(),
            "all_day": event.all_day,
            "repeat": event.repeat,
            "event_color": event.event_color,
            "reminder": event.reminder,
        }
        return self._request(
            "POST",
            "events",
            json=payload,
            headers={"Prefer": "return=representation"},
        ).json()[0]

    def delete_event(self, user_id: str, event_id: str) -> bool:
        rows = self._request(
            "DELETE",
            "events",
            params={"id": f"eq.{event_id}", "user_id": f"eq.{user_id}"},
            headers={"Prefer": "return=representation"},
        ).json()
        return bool(rows)

    def update_event(
        self, user_id: str, event_id: str, update: EventUpdate
    ) -> dict[str, Any] | None:
        changes = update.model_dump(exclude_none=True, mode="json")
        for field in ("description", "notes", "location"):
            if changes.get(field) == "":
                changes[field] = None
        if "start_time" in changes and "due_date" not in changes:
            changes["due_date"] = changes["start_time"]
        if "due_date" in changes and "start_time" not in changes:
            changes["start_time"] = changes["due_date"]
        if "description" in changes and "notes" not in changes:
            changes["notes"] = changes["description"]
        if "notes" in changes and "description" not in changes:
            changes["description"] = changes["notes"]
        rows = self._request(
            "PATCH",
            "events",
            params={"id": f"eq.{event_id}", "user_id": f"eq.{user_id}"},
            json=changes,
            headers={"Prefer": "return=representation"},
        ).json()
        return rows[0] if rows else None

    def get_settings(self, user_id: str) -> dict[str, Any]:
        rows = self._request(
            "GET", "user_settings", params={"user_id": f"eq.{user_id}", "limit": "1"}
        ).json()
        if rows:
            row = rows[0]
        else:
            defaults = {
                "user_id": user_id,
                **UserSettings().model_dump(exclude={"extension_connected"}),
            }
            row = self._request(
                "POST",
                "user_settings",
                json=defaults,
                headers={"Prefer": "return=representation"},
            ).json()[0]
        last_seen = row.pop("extension_last_seen", None)
        row["extension_connected"] = bool(
            last_seen
            and datetime.fromisoformat(last_seen.replace("Z", "+00:00"))
            >= datetime.now(timezone.utc) - timedelta(days=7)
        )
        return row

    def update_settings(
        self, user_id: str, update: UserSettingsUpdate
    ) -> dict[str, Any]:
        current = self.get_settings(user_id)
        extension_connected = current.pop("extension_connected", False)
        current.update(update.model_dump(exclude_none=True))
        rows = self._request(
            "POST",
            "user_settings",
            params={"on_conflict": "user_id"},
            json=current,
            headers={"Prefer": "resolution=merge-duplicates,return=representation"},
        ).json()
        if update.preferred_language:
            self._request(
                "PATCH",
                "users",
                params={"id": f"eq.{user_id}"},
                json={"preferred_language": update.preferred_language},
            )
        result = rows[0]
        result["extension_connected"] = extension_connected
        return result

    def mark_extension_seen(self, user_id: str) -> None:
        self._request(
            "POST",
            "user_settings",
            params={"on_conflict": "user_id"},
            json={
                "user_id": user_id,
                "extension_last_seen": datetime.now(timezone.utc).isoformat(),
            },
            headers={"Prefer": "resolution=merge-duplicates"},
        )

    def list_courses(self, user_id: str) -> list[dict[str, Any]]:
        return self._request(
            "GET", "courses", params={"user_id": f"eq.{user_id}", "order": "created_at.asc"}
        ).json()

    def create_or_update_course(self, user_id: str, course_code: str, course_name: str, teaching_plan: str | None) -> dict[str, Any]:
        payload = {
            "user_id": user_id,
            "course_code": course_code,
            "course_name": course_name,
            "teaching_plan": teaching_plan
        }
        rows = self._request(
            "POST",
            "courses",
            params={"on_conflict": "user_id,course_code"},
            json=payload,
            headers={"Prefer": "resolution=merge-duplicates,return=representation"},
        ).json()
        return rows[0]

    def delete_course(self, user_id: str, course_code: str) -> bool:
        rows = self._request(
            "DELETE",
            "courses",
            params={"course_code": f"eq.{course_code}", "user_id": f"eq.{user_id}"},
            headers={"Prefer": "return=representation"},
        ).json()
        return bool(rows)

    def list_schedule(self, user_id: str) -> list[dict[str, Any]]:
        return self._request(
            "GET", "study_schedule", params={"user_id": f"eq.{user_id}"}
        ).json()

    def create_schedule(self, user_id: str, data: dict[str, Any]) -> dict[str, Any]:
        start_time = data["start_time"]
        if hasattr(start_time, "isoformat"):
            start_time = start_time.isoformat()
        end_time = data["end_time"]
        if hasattr(end_time, "isoformat"):
            end_time = end_time.isoformat()
        payload = {
            "user_id": user_id,
            "course_code": data["course_code"],
            "day_of_week": data["day_of_week"],
            "start_time": start_time,
            "end_time": end_time,
            "location": data.get("location")
        }
        rows = self._request(
            "POST",
            "study_schedule",
            json=payload,
            headers={"Prefer": "return=representation"}
        ).json()
        return rows[0]

    def delete_schedule(self, user_id: str, slot_id: str) -> bool:
        rows = self._request(
            "DELETE",
            "study_schedule",
            params={"id": f"eq.{slot_id}", "user_id": f"eq.{user_id}"},
            headers={"Prefer": "return=representation"}
        ).json()
        return bool(rows)

    def list_alerts(self, user_id: str, status: str | None = None) -> list[dict[str, Any]]:
        params = {"user_id": f"eq.{user_id}"}
        if status:
            params["status"] = f"eq.{status}"
        return self._request(
            "GET", "verification_alerts", params=params
        ).json()

    def get_alert(self, user_id: str, alert_id: str) -> dict[str, Any] | None:
        rows = self._request(
            "GET", "verification_alerts", params={"id": f"eq.{alert_id}", "user_id": f"eq.{user_id}", "limit": "1"}
        ).json()
        return rows[0] if rows else None

    def create_alert(self, user_id: str, alert_data: dict[str, Any]) -> dict[str, Any]:
        payload = {
            "user_id": user_id,
            "event_data": alert_data["event_data"],
            "email_hash": alert_data["email_hash"],
            "alert_type": alert_data["alert_type"],
            "ai_reason": alert_data.get("ai_reason"),
            "confidence": alert_data.get("confidence", "low"),
            "status": "pending"
        }
        rows = self._request(
            "POST",
            "verification_alerts",
            json=payload,
            headers={"Prefer": "return=representation"}
        ).json()
        return rows[0]

    def update_alert_status(self, user_id: str, alert_id: str, status: str) -> dict[str, Any] | None:
        payload = {
            "status": status,
            "resolved_at": datetime.now(timezone.utc).isoformat()
        }
        rows = self._request(
            "PATCH",
            "verification_alerts",
            params={"id": f"eq.{alert_id}", "user_id": f"eq.{user_id}"},
            json=payload,
            headers={"Prefer": "return=representation"}
        ).json()
        return rows[0] if rows else None


def create_storage(settings: Settings) -> Storage:
    if settings.database_backend == "supabase":
        return SupabaseStorage(settings)
    return InMemoryStorage()
