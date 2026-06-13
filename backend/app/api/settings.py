from typing import Annotated

from fastapi import APIRouter, Depends

from app.api.dependencies import get_current_user, get_storage
from app.schemas.settings import UserSettings, UserSettingsUpdate
from app.services.storage import Storage


router = APIRouter(prefix="/api/settings", tags=["settings"])
extension_router = APIRouter(prefix="/api/extension", tags=["extension"])


@router.get("", response_model=UserSettings)
def get_user_settings(
    user: Annotated[dict, Depends(get_current_user)],
    storage: Annotated[Storage, Depends(get_storage)],
) -> UserSettings:
    return UserSettings.model_validate(storage.get_settings(user["id"]))


@router.patch("", response_model=UserSettings)
def update_user_settings(
    payload: UserSettingsUpdate,
    user: Annotated[dict, Depends(get_current_user)],
    storage: Annotated[Storage, Depends(get_storage)],
) -> UserSettings:
    return UserSettings.model_validate(storage.update_settings(user["id"], payload))


@extension_router.post("/heartbeat")
def extension_heartbeat(
    user: Annotated[dict, Depends(get_current_user)],
    storage: Annotated[Storage, Depends(get_storage)],
) -> dict[str, bool]:
    storage.mark_extension_seen(user["id"])
    return {"connected": True}
