(function startMiyadContentScript() {
  const logger = globalThis.MiyadLogger;
  const scraper = globalThis.MiyadScraper;
  let lastUrl = "";
  let observedPane = null;
  let observer = null;
  let debounceTimer = null;
  let currentEmail = null;

  logger.info("Content script initialized on page:", location.href);

  function detectCurrentEmail(pane) {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
      currentEmail = scraper.extractEmail(document, pane);
      logger.info("Scraping email context:", currentEmail);
      if (!currentEmail) return;
      chrome.runtime.sendMessage({
        type: "EMAIL_DETECTED",
        email: currentEmail
      }).then(() => {
        logger.info("Successfully reported email to background script");
      }).catch((error) => {
        logger.warn("Could not report the current email", error);
      });
    }, 350);
  }

  function attachToReadingPane() {
    const pane = scraper.findReadingPane(document);
    if (!pane) {
      logger.debug("Reading pane not found yet in DOM.");
      return;
    }
    if (pane === observedPane) return;

    logger.info("Found Outlook reading pane! Attaching observer.");
    observer?.disconnect();
    observedPane = pane;
    observer = new MutationObserver(() => detectCurrentEmail(pane));
    observer.observe(pane, {
      subtree: true,
      childList: true,
      characterData: true
    });
    detectCurrentEmail(pane);
    logger.debug("Attached observer to Outlook reading pane");
  }

  function checkReadingPaneStatus() {
    if (observedPane && !document.contains(observedPane)) {
      logger.info("Observed reading pane was detached. Resetting observer.");
      observer?.disconnect();
      observer = null;
      observedPane = null;
      currentEmail = null;
    }
    if (!observedPane) {
      attachToReadingPane();
    }
  }

  function pollUrl() {
    if (location.href === lastUrl) return;
    logger.info("URL changed from", lastUrl, "to", location.href, "- resetting observer.");
    lastUrl = location.href;
    observer?.disconnect();
    observer = null;
    observedPane = null;
    currentEmail = null;
    attachToReadingPane();
  }

  chrome.runtime.onMessage.addListener((message, _sender, sendResponse) => {
    if (message.type !== "GET_CURRENT_EMAIL") return false;
    logger.info("Received GET_CURRENT_EMAIL message request");
    const pane = scraper.findReadingPane(document);
    currentEmail = pane ? scraper.extractEmail(document, pane) : null;
    logger.info("Responding with current email:", currentEmail);
    sendResponse({ email: currentEmail });
    return false;
  });

  setInterval(pollUrl, 500);
  setInterval(checkReadingPaneStatus, 1000);
  pollUrl();
  checkReadingPaneStatus();
})();
