from typing import Annotated
from datetime import datetime, timedelta
from fastapi import APIRouter, Depends, HTTPException, status

from app.api.dependencies import get_current_user, get_storage
from app.services.storage import Storage
from app.schemas.academic import (
    CourseCreate,
    CourseResponse,
    ScheduleCreate,
    ScheduleResponse,
    VerificationAlertResponse,
    AlertResolution,
)
from app.schemas.event import EventCreate

router = APIRouter(prefix="/api", tags=["academic"])


# --- Courses Endpoints ---

@router.get("/courses", response_model=list[CourseResponse])
def list_courses(
    user: Annotated[dict, Depends(get_current_user)],
    storage: Annotated[Storage, Depends(get_storage)],
) -> list[CourseResponse]:
    rows = storage.list_courses(user["id"])
    return [CourseResponse.model_validate(row) for row in rows]


@router.post("/courses", response_model=CourseResponse, status_code=201)
def create_course(
    payload: CourseCreate,
    user: Annotated[dict, Depends(get_current_user)],
    storage: Annotated[Storage, Depends(get_storage)],
) -> CourseResponse:
    row = storage.create_or_update_course(
        user["id"],
        payload.course_code,
        payload.course_name,
        payload.teaching_plan,
    )
    return CourseResponse.model_validate(row)


@router.delete("/courses/{course_code}", status_code=200)
def delete_course(
    course_code: str,
    user: Annotated[dict, Depends(get_current_user)],
    storage: Annotated[Storage, Depends(get_storage)],
) -> dict[str, bool]:
    success = storage.delete_course(user["id"], course_code)
    if not success:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Course not found",
        )
    return {"success": True}


# --- Schedule Endpoints ---

@router.get("/schedule", response_model=list[ScheduleResponse])
def list_schedule(
    user: Annotated[dict, Depends(get_current_user)],
    storage: Annotated[Storage, Depends(get_storage)],
) -> list[ScheduleResponse]:
    rows = storage.list_schedule(user["id"])
    return [ScheduleResponse.model_validate(row) for row in rows]


@router.post("/schedule", response_model=ScheduleResponse, status_code=201)
def create_schedule(
    payload: ScheduleCreate,
    user: Annotated[dict, Depends(get_current_user)],
    storage: Annotated[Storage, Depends(get_storage)],
) -> ScheduleResponse:
    # Check if course exists first
    courses = storage.list_courses(user["id"])
    course_codes = {c["course_code"].strip().upper() for c in courses}
    if payload.course_code.strip().upper() not in course_codes:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Course '{payload.course_code}' must be created before adding to schedule.",
        )

    row = storage.create_schedule(user["id"], payload.model_dump())
    return ScheduleResponse.model_validate(row)


@router.delete("/schedule/{slot_id}", status_code=200)
def delete_schedule(
    slot_id: str,
    user: Annotated[dict, Depends(get_current_user)],
    storage: Annotated[Storage, Depends(get_storage)],
) -> dict[str, bool]:
    success = storage.delete_schedule(user["id"], slot_id)
    if not success:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Schedule slot not found",
        )
    return {"success": True}


# --- Alerts/Verification Endpoints ---

@router.get("/alerts", response_model=list[VerificationAlertResponse])
def list_alerts(
    user: Annotated[dict, Depends(get_current_user)],
    storage: Annotated[Storage, Depends(get_storage)],
    status_filter: str | None = "pending",
) -> list[VerificationAlertResponse]:
    rows = storage.list_alerts(user["id"], status_filter)
    return [VerificationAlertResponse.model_validate(row) for row in rows]


@router.post("/alerts/{alert_id}/resolve", response_model=VerificationAlertResponse)
def resolve_alert(
    alert_id: str,
    payload: AlertResolution,
    user: Annotated[dict, Depends(get_current_user)],
    storage: Annotated[Storage, Depends(get_storage)],
) -> VerificationAlertResponse:
    alert = storage.get_alert(user["id"], alert_id)
    if not alert:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Alert not found",
        )

    if alert["status"] != "pending":
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Alert is already resolved",
        )

    # Perform resolution
    if payload.action == "confirm":
        # Create event
        event_data = payload.event_data if payload.event_data is not None else alert["event_data"]
        due_date = datetime.fromisoformat(event_data["due_date"].replace("Z", "+00:00"))
        all_day = event_data.get("all_day", False)
        
        event_create = EventCreate(
            title=event_data["title"],
            description=event_data.get("notes"),
            start_time=due_date,
            end_time=due_date + (timedelta(days=1) if all_day else timedelta(hours=1)),
            all_day=all_day,
            location=event_data.get("location"),
            event_type=event_data.get("event_type", "other"),
            reminder="one_day",
            event_color="#BCDA4B",
            repeat="none"
        )
        storage.create_event(user["id"], event_create)
        status_str = "confirmed"
    else:
        status_str = "rejected"

    row = storage.update_alert_status(user["id"], alert_id, status_str)

    return VerificationAlertResponse.model_validate(row)
