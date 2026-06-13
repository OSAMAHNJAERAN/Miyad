from datetime import datetime
from typing import Literal

from pydantic import BaseModel, ConfigDict, Field, field_validator


EventType = Literal["exam", "deadline", "quiz", "lecture", "other"]


class ExtractedEvent(BaseModel):
    title: str = Field(min_length=1, max_length=300)
    course_code: str | None = Field(default=None, max_length=80)
    event_type: EventType
    due_date: datetime
    location: str | None = Field(default=None, max_length=300)
    notes: str | None = Field(default=None, max_length=2000)

    @field_validator("due_date")
    @classmethod
    def require_timezone(cls, value: datetime) -> datetime:
        if value.tzinfo is None or value.utcoffset() is None:
            raise ValueError("due_date must include a timezone")
        return value


class EventResponse(ExtractedEvent):
    model_config = ConfigDict(from_attributes=True)

    id: str
    source_hash: str | None = None
    created_at: datetime
    reminder: str | None = None


class EventsResponse(BaseModel):
    events: list[EventResponse]


class EventUpdate(BaseModel):
    title: str | None = Field(default=None, min_length=1, max_length=300)
    due_date: datetime | None = None
    notes: str | None = Field(default=None, max_length=2000)
    location: str | None = Field(default=None, max_length=300)
    reminder: Literal["same_day", "one_day", "one_week", "none"] | None = None

    @field_validator("due_date")
    @classmethod
    def require_timezone(cls, value: datetime | None) -> datetime | None:
        if value is not None and (value.tzinfo is None or value.utcoffset() is None):
            raise ValueError("due_date must include a timezone")
        return value
