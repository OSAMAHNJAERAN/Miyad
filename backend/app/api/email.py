import hashlib
from typing import Annotated

import httpx
from fastapi import APIRouter, Depends, HTTPException, status

from app.api.dependencies import get_current_user, get_extractor, get_storage
from app.schemas.email import (
    ConfirmExtractionRequest,
    ConfirmManualExtractionRequest,
    EmailMetadata,
    ManualExtractRequest,
    ProcessEmailRequest,
    ProcessEmailResponse,
)
from app.services.llm import Extractor
from app.services.storage import Storage


router = APIRouter(prefix="/api", tags=["extraction"])


async def run_extraction(
    *,
    user: dict,
    storage: Storage,
    extractor: Extractor,
    metadata: EmailMetadata,
    raw_content: str,
    email_hash: str,
    save: bool,
) -> ProcessEmailResponse:
    if save and storage.hash_exists(user["id"], email_hash):
        return ProcessEmailResponse(
            status="already_processed", events_created=0, events=[]
        )
    try:
        events = await extractor.extract(metadata, raw_content)
    except RuntimeError as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc
    except (httpx.HTTPError, ValueError) as exc:
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail="AI extraction service failed",
        ) from exc

    if not events:
        if save:
            storage.save_extraction(
                user["id"], email_hash, metadata.subject, []
            )
        return ProcessEmailResponse(
            status="no_events_found", events_created=0, events=[]
        )

    # === Smart AI Verification ===
    from app.services.verification import VerificationService
    from app.core.config import get_settings

    courses = storage.list_courses(user["id"])
    schedule = storage.list_schedule(user["id"])
    preferred_lang = user.get("preferred_language", "ar")

    verifier = VerificationService(get_settings())

    verified_events = []
    needs_review_events = []

    for event in events:
        result = await verifier.verify(event, courses, schedule, preferred_lang)
        event.verification_action = result.action
        event.verification_reason = result.reason
        if result.action == "auto_add":
            verified_events.append(event)
        else:
            needs_review_events.append((event, result.action, result.reason))

    if not save:
        return ProcessEmailResponse(
            status="preview", events_created=0, events=events
        )

    # If user has no courses defined, bypass verification and auto-add all events
    if not courses:
        rows = storage.save_extraction(
            user["id"], email_hash, metadata.subject, events
        )
        return ProcessEmailResponse(
            status="success", events_created=len(rows), events=events
        )

    # Save verified events to calendar
    if verified_events:
        rows = storage.save_extraction(
            user["id"], email_hash, metadata.subject, verified_events
        )
    else:
        storage.save_extraction(
            user["id"], email_hash, metadata.subject, []
        )
        rows = []

    # Create alerts for needs_review or not_matching events
    for event, action, reason in needs_review_events:
        alert_data = {
            "event_data": event.model_dump(mode="json"),
            "email_hash": email_hash,
            "alert_type": action,
            "ai_reason": reason,
            "confidence": event.confidence
        }
        storage.create_alert(user["id"], alert_data)

    return ProcessEmailResponse(
        status="success", events_created=len(rows), events=verified_events
    )


from app.core.rate_limit import RateLimiter


@router.post("/process-email", response_model=ProcessEmailResponse, dependencies=[Depends(RateLimiter(30, 60))])
async def process_email(
    payload: ProcessEmailRequest,
    user: Annotated[dict, Depends(get_current_user)],
    storage: Annotated[Storage, Depends(get_storage)],
    extractor: Annotated[Extractor, Depends(get_extractor)],
) -> ProcessEmailResponse:
    return await run_extraction(
        user=user,
        storage=storage,
        extractor=extractor,
        metadata=payload.metadata,
        raw_content=payload.raw_content,
        email_hash=payload.email_hash,
        save=True,
    )


@router.post(
    "/preview-email",
    response_model=ProcessEmailResponse,
    dependencies=[Depends(RateLimiter(30, 60))],
)
async def preview_email(
    payload: ProcessEmailRequest,
    user: Annotated[dict, Depends(get_current_user)],
    storage: Annotated[Storage, Depends(get_storage)],
    extractor: Annotated[Extractor, Depends(get_extractor)],
) -> ProcessEmailResponse:
    return await run_extraction(
        user=user,
        storage=storage,
        extractor=extractor,
        metadata=payload.metadata,
        raw_content=payload.raw_content,
        email_hash=payload.email_hash,
        save=False,
    )


@router.post(
    "/confirm-extraction",
    response_model=ProcessEmailResponse,
    dependencies=[Depends(RateLimiter(30, 60))],
)
async def confirm_extraction(
    payload: ConfirmExtractionRequest,
    user: Annotated[dict, Depends(get_current_user)],
    storage: Annotated[Storage, Depends(get_storage)],
) -> ProcessEmailResponse:
    if storage.hash_exists(user["id"], payload.email_hash):
        return ProcessEmailResponse(
            status="already_processed", events_created=0, events=[]
        )
    rows = storage.save_extraction(
        user["id"],
        payload.email_hash,
        payload.metadata.subject,
        payload.events,
    )
    return ProcessEmailResponse(
        status="success" if rows else "already_processed",
        events_created=len(rows),
        events=payload.events if rows else [],
    )


@router.post(
    "/confirm-manual-extraction",
    response_model=ProcessEmailResponse,
    dependencies=[Depends(RateLimiter(10, 60))],
)
async def confirm_manual_extraction(
    payload: ConfirmManualExtractionRequest,
    user: Annotated[dict, Depends(get_current_user)],
    storage: Annotated[Storage, Depends(get_storage)],
) -> ProcessEmailResponse:
    digest_source = (
        f"{user['id']}|{payload.subject}|{payload.sender}|{payload.raw_content}"
    )
    email_hash = "sha256:" + hashlib.sha256(
        digest_source.encode("utf-8")
    ).hexdigest()
    if storage.hash_exists(user["id"], email_hash):
        return ProcessEmailResponse(
            status="already_processed", events_created=0, events=[]
        )
    rows = storage.save_extraction(
        user["id"], email_hash, payload.subject, payload.events
    )
    return ProcessEmailResponse(
        status="success" if rows else "already_processed",
        events_created=len(rows),
        events=payload.events if rows else [],
    )


@router.post("/extract-manual", response_model=ProcessEmailResponse, dependencies=[Depends(RateLimiter(10, 60))])
async def extract_manual(
    payload: ManualExtractRequest,
    user: Annotated[dict, Depends(get_current_user)],
    storage: Annotated[Storage, Depends(get_storage)],
    extractor: Annotated[Extractor, Depends(get_extractor)],
) -> ProcessEmailResponse:
    metadata = EmailMetadata(
        sender=payload.sender,
        subject=payload.subject,
        timestamp=payload.timestamp,
        timezone=payload.timezone,
    )
    digest_source = (
        f"{user['id']}|{payload.subject}|{payload.sender}|{payload.raw_content}"
    )
    email_hash = "sha256:" + hashlib.sha256(
        digest_source.encode("utf-8")
    ).hexdigest()
    return await run_extraction(
        user=user,
        storage=storage,
        extractor=extractor,
        metadata=metadata,
        raw_content=payload.raw_content,
        email_hash=email_hash,
        save=payload.save,
    )
