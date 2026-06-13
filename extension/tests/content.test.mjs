import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import test from "node:test";

test("content script polls navigation and observes only the reading pane", async () => {
  const source = await readFile(
    new URL("../src/content.js", import.meta.url),
    "utf8"
  );

  assert.match(source, /setInterval\(pollUrl,\s*500\)/);
  assert.match(source, /observer\.observe\(pane,/);
  assert.doesNotMatch(source, /observer\.observe\(document\.body,/);
});
