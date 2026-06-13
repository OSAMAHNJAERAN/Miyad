from typing import Annotated

from fastapi import Depends, HTTPException, Request, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer

from app.core.security import decode_access_token
from app.services.llm import Extractor
from app.services.storage import Storage


bearer = HTTPBearer(auto_error=False)


def get_storage(request: Request) -> Storage:
    return request.app.state.storage


def get_extractor(request: Request) -> Extractor:
    return request.app.state.extractor


def get_current_user(
    request: Request,
    credentials: Annotated[HTTPAuthorizationCredentials | None, Depends(bearer)],
    storage: Annotated[Storage, Depends(get_storage)],
) -> dict:
    if credentials is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, detail="Authentication required"
        )
    payload = decode_access_token(
        credentials.credentials, request.app.state.settings.jwt_secret
    )
    user = storage.get_user(payload["sub"])
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, detail="User no longer exists"
        )
    return user
