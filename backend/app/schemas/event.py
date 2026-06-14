from datetime import datetime
from typing import Literal

from pydantic import BaseModel, ConfigDict, Field, field_validator, model_validator


EventType = Literal["exam", "deadline", "quiz", "lecture", "other"]
ReminderType = Literal["same_day", "one_day", "one_week", "none"]
RepeatType = Literal["none", "daily", "weekly", "monthly", "custom"]


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
    description: str | None = None
    start_time: datetime | None = None
    end_time: datetime | None = None
    all_day: bool = False
    repeat: RepeatType = "none"
    event_color: str = "#B8F23A"


class EventsResponse(BaseModel):
    events: list[EventResponse]


class EventCreate(BaseModel):
    title: str = Field(min_length=1, max_length=300)
    description: str | None = Field(default=None, max_length=2000)
    start_time: datetime
    end_time: datetime
    all_day: bool = False
    repeat: RepeatType = "none"
    location: str | None = Field(default=None, max_length=300)
    reminder: ReminderType = "one_day"
    event_color: str = Field(default="#B8F23A", pattern=r"^#[0-9A-Fa-f]{6}$")
    event_type: EventType = "other"

    @field_validator("start_time", "end_time")
    @classmethod
    def require_timezone(cls, value: datetime) -> datetime:
        if value.tzinfo is None or value.utcoffset() is None:
            raise ValueError("event times must include a timezone")
        return value

    @model_validator(mode="after")
    def validate_time_range(self) -> "EventCreate":
        if self.end_time <= self.start_time:
            raise ValueError("end_time must be after start_time")
        return self


class EventUpdate(BaseModel):
    title: str | None = Field(default=None, min_length=1, max_length=300)
    description: str | None = Field(default=None, max_length=2000)
    due_date: datetime | None = None
    start_time: datetime | None = None
    end_time: datetime | None = None
    all_day: bool | None = None
    repeat: RepeatType | None = None
    notes: str | None = Field(default=None, max_length=2000)
    location: str | None = Field(default=None, max_length=300)
    reminder: ReminderType | None = None
    event_color: str | None = Field(
        default=None, pattern=r"^#[0-9A-Fa-f]{6}$"
    )

    @field_validator("due_date", "start_time", "end_time")
    @classmethod
    def require_timezone(cls, value: datetime | None) -> datetime | None:
        if value is not None and (value.tzinfo is None or value.utcoffset() is None):
            raise ValueError("event times must include a timezone")
        return value
