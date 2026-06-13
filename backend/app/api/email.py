import hashlib
from typing import Annotated

import httpx
from fastapi import APIRouter, Depends, HTTPException, status

from app.api.dependencies import get_current_user, get_extractor, get_storage
from app.schemas.email import (
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
    if not save:
        return ProcessEmailResponse(
            status="preview", events_created=0, events=events
        )
    rows = storage.save_extraction(
        user["id"], email_hash, metadata.subject, events
    )
    if not events:
        return ProcessEmailResponse(
            status="no_events_found", events_created=0, events=[]
        )
    if not rows:
        return ProcessEmailResponse(
            status="already_processed", events_created=0, events=[]
        )
    return ProcessEmailResponse(
        status="success", events_created=len(rows), events=events
    )


@router.post("/process-email", response_model=ProcessEmailResponse)
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


@router.post("/extract-manual", response_model=ProcessEmailResponse)
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
        f"{user['id']}|{payload.subject}|{payload.sender}|"
        f"{payload.timestamp.isoformat()}|{payload.raw_content}"
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
