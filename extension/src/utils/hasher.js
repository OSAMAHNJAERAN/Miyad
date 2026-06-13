export async function sha256(value) {
  const bytes = new TextEncoder().encode(value);
  const digest = await crypto.subtle.digest("SHA-256", bytes);
  const hex = [...new Uint8Array(digest)]
    .map((byte) => byte.toString(16).padStart(2, "0"))
    .join("");
  return `sha256:${hex}`;
}

export function emailFingerprintSource(email) {
  return [
    email.subject || "",
    email.sender || "",
    email.timestamp || "",
    (email.body || "").slice(0, 100)
  ].join("");
}
