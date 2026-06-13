(function startMiyadContentScript() {
  const logger = globalThis.MiyadLogger;
  const scraper = globalThis.MiyadScraper;
  let lastUrl = "";
  let observedPane = null;
  let observer = null;
  let debounceTimer = null;

  function sendCurrentEmail(pane) {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
      const email = scraper.extractEmail(document, pane);
      if (!email) return;
      chrome.runtime.sendMessage({ type: "PROCESS_EMAIL", email }).catch((error) => {
        logger.warn("Could not send email to service worker", error);
      });
    }, 350);
  }

  function attachToReadingPane() {
    const pane = scraper.findReadingPane(document);
    if (!pane || pane === observedPane) return;

    observer?.disconnect();
    observedPane = pane;
    observer = new MutationObserver(() => sendCurrentEmail(pane));
    observer.observe(pane, {
      subtree: true,
      childList: true,
      characterData: true
    });
    sendCurrentEmail(pane);
    logger.debug("Attached observer to Outlook reading pane");
  }

  function pollUrl() {
    if (location.href === lastUrl) return;
    lastUrl = location.href;
    observer?.disconnect();
    observer = null;
    observedPane = null;

    let attempts = 0;
    const locator = setInterval(() => {
      attempts += 1;
      attachToReadingPane();
      if (observedPane || attempts >= 20) clearInterval(locator);
    }, 250);
  }

  setInterval(pollUrl, 500);
  pollUrl();
})();
