from datetime import datetime

from pydantic import BaseModel, Field, field_validator

from app.schemas.event import ExtractedEvent


class EmailMetadata(BaseModel):
    sender: str = Field(default="", max_length=320)
    subject: str = Field(default="", max_length=500)
    timestamp: datetime
    timezone: str = Field(min_length=1, max_length=100)


class ProcessEmailRequest(BaseModel):
    metadata: EmailMetadata
    raw_content: str = Field(min_length=1, max_length=100_000)
    email_hash: str = Field(pattern=r"^sha256:[a-fA-F0-9]{64}$")

    @field_validator("email_hash")
    @classmethod
    def normalize_hash(cls, value: str) -> str:
        return value.lower()


class ManualExtractRequest(BaseModel):
    raw_content: str = Field(min_length=1, max_length=100_000)
    subject: str = Field(default="Manual extraction", max_length=500)
    sender: str = Field(default="manual", max_length=320)
    timestamp: datetime
    timezone: str = Field(min_length=1, max_length=100)
    save: bool = True


class ProcessEmailResponse(BaseModel):
    status: str
    events_created: int
    events: list[ExtractedEvent] = Field(default_factory=list)
