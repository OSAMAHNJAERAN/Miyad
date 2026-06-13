import assert from "node:assert/strict";
import test from "node:test";
import { emailFingerprintSource, sha256 } from "../src/utils/hasher.js";

test("fingerprint follows the required subject sender timestamp body prefix order", () => {
  const source = emailFingerprintSource({
    subject: "Subject",
    sender: "sender@example.edu",
    timestamp: "2026-06-11T10:00:00+08:00",
    body: "x".repeat(150)
  });
  assert.equal(
    source,
    `Subjectsender@example.edu2026-06-11T10:00:00+08:00${"x".repeat(100)}`
  );
});

test("sha256 is stable and prefixed", async () => {
  assert.equal(
    await sha256("Miyad"),
    "sha256:7e910a8753d6c73cd7822801a7f95101517aa2c35c31729857b6d379910dc0c2"
  );
});
