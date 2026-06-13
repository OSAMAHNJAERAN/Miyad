import assert from "node:assert/strict";
import test from "node:test";
import "../src/utils/scraper.js";

const scraper = globalThis.MiyadScraper;

function element({ text = "", attrs = {}, matches = {} } = {}) {
  return {
    innerText: text,
    textContent: text,
    getAttribute(name) {
      return attrs[name] || null;
    },
    querySelector(selector) {
      return matches[selector] || null;
    }
  };
}

test("extractEmail supports fallback selectors and timezone metadata", () => {
  const body = element({ text: "Submit the assignment by Friday." });
  const subject = element({ text: "Database Assignment" });
  const sender = element({ attrs: { title: "lecturer@university.edu" } });
  const timestamp = element({
    attrs: { datetime: "2026-06-11T10:00:00+08:00" }
  });
  const documentRef = element({
    matches: {
      ".ReadingPaneContents": body,
      ".SubjectHeader": subject
    }
  });
  body.querySelector = (selector) =>
    ({
      ".ms-Persona-primaryText": sender,
      "time[datetime]": timestamp
    })[selector] || null;

  const result = scraper.extractEmail(documentRef, body);
  assert.equal(result.subject, "Database Assignment");
  assert.equal(result.sender, "lecturer@university.edu");
  assert.equal(result.body, "Submit the assignment by Friday.");
  assert.ok(result.timezone);
});
