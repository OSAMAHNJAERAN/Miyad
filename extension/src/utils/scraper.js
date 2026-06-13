(function installScraper(root) {
  const SELECTORS = {
    emailBody: [
      '[aria-label="Message body"]',
      ".ReadingPaneContents",
      '[data-testid="message-body"]',
      ".allowTextSelection"
    ],
    subject: [
      ".SubjectHeader",
      '[data-testid="ConversationReadingPaneSubject"]',
      'h1[aria-level="2"]',
      '[role="heading"].allowTextSelection',
      "div.JdFsz"
    ],
    sender: [
      '[aria-label*="From"]',
      ".ms-Persona-primaryText",
      '[data-testid="senderName"]',
      'span[title*="@"]'
    ],
    timestamp: [
      "time[datetime]",
      '[data-testid="sent-date"]',
      ".metadata-timestamp",
      "div.srQCs"
    ]
  };

  function firstMatch(scope, selectors) {
    for (const selector of selectors) {
      const element = scope.querySelector(selector);
      if (element) return element;
    }
    return null;
  }

  function valueOf(element, attribute) {
    if (!element) return "";
    if (attribute) {
      const value = element.getAttribute(attribute);
      if (value) return value.trim();
    }
    return (element.innerText || element.textContent || "").trim();
  }

  function findReadingPane(documentRef) {
    return firstMatch(documentRef, SELECTORS.emailBody);
  }

  function extractEmail(documentRef, pane) {
    const bodyElement = firstMatch(pane, SELECTORS.emailBody) || pane;
    const subjectElement =
      firstMatch(documentRef, SELECTORS.subject) ||
      firstMatch(pane, SELECTORS.subject);
    const senderElement =
      firstMatch(pane, SELECTORS.sender) ||
      firstMatch(documentRef, SELECTORS.sender);
    const timestampElement =
      firstMatch(pane, SELECTORS.timestamp) ||
      firstMatch(documentRef, SELECTORS.timestamp);

    const body = valueOf(bodyElement);
    if (!body) return null;

    return {
      subject: valueOf(subjectElement),
      sender:
        valueOf(senderElement, "title") ||
        valueOf(senderElement, "aria-label") ||
        valueOf(senderElement),
      timestamp:
        valueOf(timestampElement, "datetime") ||
        valueOf(timestampElement) ||
        new Date().toISOString(),
      timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
      body
    };
  }

  const api = { SELECTORS, firstMatch, findReadingPane, extractEmail };
  root.MiyadScraper = api;
  if (typeof module !== "undefined" && module.exports) module.exports = api;
})(globalThis);
