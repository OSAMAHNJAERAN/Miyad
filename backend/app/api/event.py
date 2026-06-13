from datetime import datetime
from typing import Annotated, Literal

from fastapi import APIRouter, Depends, HTTPException

from app.api.dependencies import get_current_user, get_storage
from app.schemas.event import EventResponse, EventsResponse, EventUpdate
from app.services.storage import Storage


router = APIRouter(prefix="/api/events", tags=["events"])


@router.get("", response_model=EventsResponse)
def list_events(
    user: Annotated[dict, Depends(get_current_user)],
    storage: Annotated[Storage, Depends(get_storage)],
    from_date: datetime | None = None,
    to_date: datetime | None = None,
    type: Literal["exam", "deadline", "quiz", "lecture", "other"] | None = None,
    language: Literal["ar", "en"] | None = None,
) -> EventsResponse:
    del language
    rows = storage.list_events(user["id"], from_date, to_date, type)
    return EventsResponse(events=[EventResponse.model_validate(row) for row in rows])


@router.delete("/{event_id}")
def delete_event(
    event_id: str,
    user: Annotated[dict, Depends(get_current_user)],
    storage: Annotated[Storage, Depends(get_storage)],
) -> dict[str, bool]:
    if not storage.delete_event(user["id"], event_id):
        raise HTTPException(status_code=404, detail="Event not found")
    return {"success": True}


@router.patch("/{event_id}", response_model=EventResponse)
def update_event(
    event_id: str,
    payload: EventUpdate,
    user: Annotated[dict, Depends(get_current_user)],
    storage: Annotated[Storage, Depends(get_storage)],
) -> EventResponse:
    row = storage.update_event(user["id"], event_id, payload)
    if not row:
        raise HTTPException(status_code=404, detail="Event not found")
    return EventResponse.model_validate(row)
