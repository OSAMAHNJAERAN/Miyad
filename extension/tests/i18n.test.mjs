import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import test from "node:test";
import {
  dictionaries,
  normalizeLanguage,
  translate
} from "../src/popup/i18n.js";

test("Arabic and English dictionaries expose the same complete key set", () => {
  assert.deepEqual(
    Object.keys(dictionaries.ar).sort(),
    Object.keys(dictionaries.en).sort()
  );
  assert.ok(Object.keys(dictionaries.en).length >= 40);
});

test("locale normalization supports Arabic and English browser locales", () => {
  assert.equal(normalizeLanguage("ar-SA"), "ar");
  assert.equal(normalizeLanguage("en-US"), "en");
  assert.equal(normalizeLanguage("ms-MY"), "en");
});

test("translations interpolate retry counts", () => {
  assert.match(translate("en", "queuedMany", { count: 3 }), /3/);
  assert.match(translate("ar", "queuedMany", { count: 3 }), /3/);
});

test("popup bindings, local assets, themes, and reduced motion are complete", async () => {
  const html = await readFile(
    new URL("../src/popup/popup.html", import.meta.url),
    "utf8"
  );
  const css = await readFile(
    new URL("../src/popup/popup.css", import.meta.url),
    "utf8"
  );
  const keys = [...html.matchAll(/data-i18n(?:-placeholder|-aria)?="([^"]+)"/g)]
    .map((match) => match[1]);

  for (const key of keys) {
    assert.ok(dictionaries.en[key], `Missing English key: ${key}`);
    assert.ok(dictionaries.ar[key], `Missing Arabic key: ${key}`);
  }
  assert.match(html, /icons\/icon-128\.png/);
  assert.doesNotMatch(html, /class="mark"/);
  assert.match(css, /\[data-theme="dark"\]/);
  assert.match(css, /prefers-color-scheme:\s*dark/);
  assert.match(css, /prefers-reduced-motion:\s*reduce/);
  assert.match(css, /backdrop-filter:\s*blur/);
});
