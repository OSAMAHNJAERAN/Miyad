import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import test from "node:test";

test("manifest is MV3 with OWA hosts, service worker, and required icons", async () => {
  const manifest = JSON.parse(
    await readFile(new URL("../manifest.json", import.meta.url), "utf8")
  );

  assert.equal(manifest.manifest_version, 3);
  assert.equal(manifest.background.type, "module");
  assert.equal(manifest.background.service_worker, "src/background.js");
  assert.ok(manifest.host_permissions.includes("https://outlook.office.com/*"));
  assert.ok(manifest.host_permissions.includes("https://outlook.live.com/*"));
  assert.deepEqual(Object.keys(manifest.icons).sort(), ["128", "16", "48"]);
  assert.ok(manifest.permissions.includes("contextMenus"));
  assert.ok(manifest.permissions.includes("alarms"));
  assert.ok(manifest.permissions.includes("storage"));
});
