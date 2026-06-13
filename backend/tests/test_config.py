import pytest
from pydantic import ValidationError

from app.core.config import Settings


def test_production_requires_real_service_configuration() -> None:
    with pytest.raises(ValidationError, match="Production configuration is incomplete"):
        Settings(
            environment="production",
            database_backend="memory",
            jwt_secret="development-only-change-me",
            cors_origins="*",
            supabase_url="",
            supabase_service_key="",
            openrouter_api_key="",
        )


def test_complete_production_configuration_is_accepted() -> None:
    settings = Settings(
        environment="production",
        database_backend="supabase",
        jwt_secret="a-production-secret-that-is-long-enough",
        cors_origins="chrome-extension://extension-id",
        supabase_url="https://example.supabase.co",
        supabase_service_key="service-key",
        openrouter_api_key="openrouter-key",
    )
    assert settings.database_backend == "supabase"
