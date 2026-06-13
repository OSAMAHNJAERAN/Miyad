from typing import Annotated

from fastapi import APIRouter, Depends, HTTPException, Request, status

from app.api.dependencies import get_storage
from app.core.security import create_access_token, hash_password, verify_password
from app.schemas.auth import AuthResponse, LoginRequest, RegisterRequest, UserResponse
from app.services.storage import Storage


router = APIRouter(prefix="/api/auth", tags=["authentication"])


def public_user(user: dict) -> UserResponse:
    return UserResponse.model_validate(user)


@router.post(
    "/register", response_model=AuthResponse, status_code=status.HTTP_201_CREATED
)
def register(
    payload: RegisterRequest,
    request: Request,
    storage: Annotated[Storage, Depends(get_storage)],
) -> AuthResponse:
    if storage.get_user_by_email(str(payload.email)):
        raise HTTPException(status_code=409, detail="Email is already registered")
    try:
        user = storage.create_user(
            {
                "email": str(payload.email).lower(),
                "password_hash": hash_password(payload.password),
                "name": payload.name.strip(),
                "university": payload.university.strip(),
                "preferred_language": "ar",
            }
        )
    except ValueError as exc:
        raise HTTPException(status_code=409, detail="Email is already registered") from exc
    return AuthResponse(
        user=public_user(user),
        token=create_access_token(user["id"], request.app.state.settings.jwt_secret),
    )


@router.post("/login", response_model=AuthResponse)
def login(
    payload: LoginRequest,
    request: Request,
    storage: Annotated[Storage, Depends(get_storage)],
) -> AuthResponse:
    user = storage.get_user_by_email(str(payload.email))
    if not user or not verify_password(payload.password, user["password_hash"]):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid email or password",
        )
    return AuthResponse(
        user=public_user(user),
        token=create_access_token(user["id"], request.app.state.settings.jwt_secret),
    )
