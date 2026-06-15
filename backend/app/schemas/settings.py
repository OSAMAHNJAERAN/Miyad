from typing import Literal

from pydantic import BaseModel


class UserSettings(BaseModel):
    preferred_language: Literal["ar", "en"] = "ar"
    notifications_enabled: bool = True
    reminder_same_day: bool = True
    reminder_one_day: bool = True
    reminder_one_week: bool = False
    extension_connected: bool = False
    registered_courses: list[str] = []


class UserSettingsUpdate(BaseModel):
    preferred_language: Literal["ar", "en"] | None = None
    notifications_enabled: bool | None = None
    reminder_same_day: bool | None = None
    reminder_one_day: bool | None = None
    reminder_one_week: bool | None = None
    registered_courses: list[str] | None = None
