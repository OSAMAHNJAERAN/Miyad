import puppeteer from 'puppeteer';
import path from 'path';
import fs from 'fs';
import { fileURLToPath } from 'url';
import http from 'http';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const extensionPath = path.resolve(__dirname, '.');

// Modify manifest to allow localhost
const manifestPath = path.resolve(__dirname, 'manifest.json');
const originalManifest = fs.readFileSync(manifestPath, 'utf8');
const manifest = JSON.parse(originalManifest);
manifest.host_permissions.push("http://localhost:8080/*");
manifest.content_scripts[0].matches.push("http://localhost:8080/*");
fs.writeFileSync(manifestPath, JSON.stringify(manifest, null, 2));

const server = http.createServer((req, res) => {
  res.writeHead(200, { 'Content-Type': 'text/html' });
  res.end(`
    <html>
      <head><title>Mock Outlook</title></head>
      <body>
        <h1 aria-level="2" class="SubjectHeader">Important Exam Schedule</h1>
        <span class="ms-Persona-primaryText" title="prof@university.edu">Professor</span>
        <time datetime="2026-06-20T10:00:00+08:00">Yesterday</time>
        <div class="ReadingPaneContents" aria-label="Message body">
          The final exam for Database Systems is tomorrow at 2 PM.
        </div>
      </body>
    </html>
  `);
});

server.listen(8080, async () => {
  try {
    console.log("Launching browser with Miyad Extension...");
    const browser = await puppeteer.launch({
      headless: "new",
      args: [
        `--disable-extensions-except=${extensionPath}`,
        `--load-extension=${extensionPath}`,
        '--no-sandbox',
        '--disable-setuid-sandbox'
      ]
    });

    const page = await browser.newPage();
    
    // Wait for extension to load
    await new Promise(r => setTimeout(r, 2000));
    
    console.log("Navigating to mock Outlook page...");
    await page.goto("http://localhost:8080/");

    console.log("Waiting for content script extraction...");
    await new Promise(r => setTimeout(r, 3000));

    console.log("Checking background service worker...");
    const targets = await browser.targets();
    const backgroundTarget = targets.find(target => target.type() === 'service_worker');
    
    if (backgroundTarget) {
        console.log("Background service worker found.");
        const worker = await backgroundTarget.worker();
        
        const result = await worker.evaluate(async () => {
            const [tab] = await chrome.tabs.query({ active: true, lastFocusedWindow: true });
            if (!tab) return { error: "No active tab" };
            
            return new Promise(resolve => {
                chrome.tabs.sendMessage(tab.id, { type: "GET_CURRENT_EMAIL" }, response => {
                    if (chrome.runtime.lastError) {
                        resolve({ error: chrome.runtime.lastError.message });
                    } else {
                        resolve(response);
                    }
                });
            });
        });
        console.log("Extraction Result from Extension:");
        console.dir(result);

        if (result && result.email && result.email.body && result.email.body.includes("final exam")) {
          console.log("SUCCESS: Extension fully works and extracted the email!");
        } else {
          console.error("FAILED: Did not extract the email correctly!");
        }
    } else {
        console.error("Background service worker NOT found!");
    }

    await browser.close();
  } catch (error) {
    console.error(error);
  } finally {
    server.close();
    fs.writeFileSync(manifestPath, originalManifest);
    console.log("Test complete.");
  }
});
