from functools import lru_cache
from typing import Literal

from pydantic import Field, model_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    app_name: str = "Miyad Academic Assistant"
    environment: Literal["development", "test", "production"] = "development"
    database_backend: Literal["memory", "supabase"] = "memory"
    jwt_secret: str = Field(default="development-only-change-me", min_length=16)
    cors_origins: str = "*"

    supabase_url: str = ""
    supabase_service_key: str = ""

    openrouter_api_key: str = ""
    openrouter_model: str = "google/gemma-2-9b-it:free"
    openrouter_http_referer: str = "https://miyad.app"
    openrouter_x_title: str = "Miyad Academic Assistant"
    openrouter_timeout_seconds: float = 30.0

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )

    @model_validator(mode="after")
    def validate_production_configuration(self) -> "Settings":
        if self.environment != "production":
            return self
        missing = []
        if self.database_backend != "supabase":
            missing.append("DATABASE_BACKEND=supabase")
        if self.jwt_secret == "development-only-change-me":
            missing.append("JWT_SECRET")
        if not self.supabase_url:
            missing.append("SUPABASE_URL")
        if not self.supabase_service_key:
            missing.append("SUPABASE_SERVICE_KEY")
        if not self.openrouter_api_key:
            missing.append("OPENROUTER_API_KEY")
        if self.cors_origins.strip() == "*":
            missing.append("CORS_ORIGINS")
        if missing:
            raise ValueError(
                "Production configuration is incomplete: " + ", ".join(missing)
            )
        return self

    @property
    def cors_origin_list(self) -> list[str]:
        if self.cors_origins.strip() == "*":
            return ["*"]
        return [item.strip() for item in self.cors_origins.split(",") if item.strip()]


@lru_cache
def get_settings() -> Settings:
    return Settings()
