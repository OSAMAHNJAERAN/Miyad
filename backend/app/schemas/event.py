from datetime import datetime
from typing import Literal

from pydantic import BaseModel, ConfigDict, Field, field_validator, model_validator


EventType = Literal["exam", "deadline", "quiz", "lecture", "other"]
ConfidenceLevel = Literal["high", "medium", "low"]
ReminderType = Literal["same_day", "one_day", "one_week", "none"]
RepeatType = Literal["none", "daily", "weekly", "monthly", "custom"]


class ExtractedEvent(BaseModel):
    title: str = Field(min_length=1, max_length=300)
    course_code: str | None = Field(default=None, max_length=80)
    course_name: str | None = Field(default=None, max_length=200)
    assignment_name: str | None = Field(default=None, max_length=300)
    event_type: EventType
    due_date: datetime
    all_day: bool = False
    location: str | None = Field(default=None, max_length=300)
    lecturer: str | None = Field(default=None, max_length=200)
    notes: str | None = Field(default=None, max_length=2000)
    confidence: ConfidenceLevel = "medium"
    source_email_subject: str | None = Field(default=None, max_length=500)
    source_email_sender: str | None = Field(default=None, max_length=320)
    evidence: str | None = Field(default=None, max_length=1000)
    verification_action: str | None = Field(default=None, max_length=50)
    verification_reason: str | None = Field(default=None, max_length=1000)

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
    event_color: str = "#BCDA4B"


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
    event_color: str = Field(default="#BCDA4B", pattern=r"^#[0-9A-Fa-f]{6}$")
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
    event_type: EventType | None = None
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

    @model_validator(mode="after")
    def validate_time_range(self) -> "EventUpdate":
        if (
            self.start_time is not None
            and self.end_time is not None
            and self.end_time <= self.start_time
        ):
            raise ValueError("end_time must be after start_time")
        return self
