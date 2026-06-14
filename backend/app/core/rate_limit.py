import time
from collections import defaultdict
from fastapi import Request, HTTPException, status

# In-memory dictionary to store rate limit timestamps
# Key: "client_ip:endpoint_path", Value: list of floats (timestamps)
_rate_limit_store = defaultdict(list)

class RateLimiter:
    def __init__(self, limit: int, seconds: int):
        self.limit = limit
        self.seconds = seconds

    async def __call__(self, request: Request):
        # Determine client IP address
        client_ip = "unknown"
        if request.client:
            client_ip = request.client.host
        
        # Handle reverse proxy headers if behind one (e.g. Cloudflare or Nginx)
        forwarded_for = request.headers.get("X-Forwarded-For")
        if forwarded_for:
            client_ip = forwarded_for.split(",")[0].strip()

        key = f"{client_ip}:{request.url.path}"
        now = time.time()

        # Evict timestamps older than rate limit window
        _rate_limit_store[key] = [t for t in _rate_limit_store[key] if now - t < self.seconds]

        # Check if limit exceeded
        if len(_rate_limit_store[key]) >= self.limit:
            raise HTTPException(
                status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                detail="Too many requests. Please try again later.",
            )

        # Add current timestamp
        _rate_limit_store[key].append(now)


def reset_rate_limiter():
    _rate_limit_store.clear()

