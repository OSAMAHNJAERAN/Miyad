from datetime import datetime, time
from typing import Literal
from pydantic import BaseModel, Field, ConfigDict


class CourseCreate(BaseModel):
    course_code: str = Field(min_length=1, max_length=80)
    course_name: str = Field(min_length=1, max_length=200)
    teaching_plan: str | None = Field(default=None, max_length=10000)


class CourseResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: str
    user_id: str
    course_code: str
    course_name: str
    teaching_plan: str | None = None
    created_at: datetime


class ScheduleCreate(BaseModel):
    course_code: str = Field(min_length=1, max_length=80)
    day_of_week: Literal['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday']
    start_time: time
    end_time: time
    location: str | None = Field(default=None, max_length=300)


class ScheduleResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: str
    user_id: str
    course_code: str
    day_of_week: str
    start_time: time
    end_time: time
    location: str | None = None
    created_at: datetime


class VerificationAlertResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: str
    user_id: str
    event_data: dict
    email_hash: str
    alert_type: Literal['needs_review', 'not_matching']
    ai_reason: str | None = None
    confidence: str
    status: Literal['pending', 'confirmed', 'rejected']
    created_at: datetime
    resolved_at: datetime | None = None


class AlertResolution(BaseModel):
    action: Literal['confirm', 'reject']
