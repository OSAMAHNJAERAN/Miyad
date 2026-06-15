from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api import academic, auth, email, event, settings
from app.core.config import Settings, get_settings
from app.services.llm import Extractor, OpenRouterExtractor
from app.services.storage import Storage, create_storage


def create_app(
    settings_override: Settings | None = None,
    storage_override: Storage | None = None,
    extractor_override: Extractor | None = None,
) -> FastAPI:
    app_settings = settings_override or get_settings()

    @asynccontextmanager
    async def lifespan(app: FastAPI):
        app.state.settings = app_settings
        app.state.storage = storage_override or create_storage(app_settings)
        if extractor_override:
            app.state.extractor = extractor_override
        elif app_settings.openrouter_api_key:
            app.state.extractor = OpenRouterExtractor(app_settings)
        elif app_settings.environment == "development":
            from app.services.llm import FakeExtractor
            app.state.extractor = FakeExtractor()
        else:
            from app.services.llm import UnavailableExtractor
            app.state.extractor = UnavailableExtractor()
        yield

    app = FastAPI(
        title=app_settings.app_name,
        version="1.0.0",
        lifespan=lifespan,
    )
    app.add_middleware(
        CORSMiddleware,
        allow_origins=app_settings.cors_origin_list,
        allow_credentials=app_settings.cors_origin_list != ["*"],
        allow_methods=["*"],
        allow_headers=["*"],
    )

    @app.get("/health", tags=["health"])
    def health() -> dict[str, str | bool]:
        return {
            "status": "ok",
            "environment": app_settings.environment,
            "database": app_settings.database_backend,
            "ai_configured": bool(app_settings.openrouter_api_key or extractor_override),
        }

    app.include_router(auth.router)
    app.include_router(email.router)
    app.include_router(event.router)
    app.include_router(academic.router)
    app.include_router(settings.router)
    app.include_router(settings.extension_router)
    return app


app = create_app()
