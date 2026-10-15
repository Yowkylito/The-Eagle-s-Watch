// Screenshots of the web build at phone and wide widths, for design review.
//
// Usage: node tools/screenshots.js <url> <outdir>
// Serve composeApp/build/dist/wasmJs/productionExecutable first, e.g. with `python3 -m http.server`.
// Requires the playwright package and its Chromium (npx playwright install chromium).
const { chromium } = require('playwright');
const fs = require('fs');
const path = require('path');

const url = process.argv[2] || 'http://127.0.0.1:8765/';
const out = process.argv[3] || 'docs/review';
fs.mkdirSync(out, { recursive: true });

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

async function shot(page, name) {
  await sleep(900);
  await page.screenshot({ path: path.join(out, name + '.png') });
  console.log('saved', name);
}

(async () => {
  const launch = { args: ['--no-sandbox'] };
  if (process.env.CHROMIUM_PATH) launch.executablePath = process.env.CHROMIUM_PATH;
  const browser = await chromium.launch(launch);
  try {
    // Phone: a 360 x 780 window, the most common Android size class.
    const phone = await browser.newContext({ viewport: { width: 360, height: 780 }, deviceScaleFactor: 2, hasTouch: true });
    const page = await phone.newPage();
    page.on('pageerror', (e) => console.log('page error:', e.message));
    page.on('console', (m) => { if (m.type() === 'error') console.log('console error:', m.text()); });
    await page.goto(url, { waitUntil: 'load' });
    await sleep(10000); // Wasm boot and font loading.
    await shot(page, 'phone-01-explore');
    await page.mouse.wheel(0, 700);
    await shot(page, 'phone-02-explore-scrolled');
    await page.mouse.wheel(0, -3000);
    await sleep(600);
    await page.mouse.click(300, 72); // Filters
    await shot(page, 'phone-03-filters');
    await page.mouse.wheel(0, 600);
    await shot(page, 'phone-04-filters-scrolled');
    await page.mouse.click(26, 26); // Close
    await sleep(800);
    await page.mouse.click(180, 350); // First project row
    await shot(page, 'phone-05-project');
    for (let i = 6; i <= 12; i++) {
      await page.mouse.wheel(0, 700);
      await shot(page, 'phone-' + String(i).padStart(2, '0') + '-project');
    }
    await page.mouse.wheel(0, -9000);
    await sleep(600);
    await page.mouse.click(26, 26); // Back
    await sleep(800);
    await page.mouse.click(135, 752); // Watchlist tab
    await shot(page, 'phone-13-watchlist');
    await page.mouse.click(225, 752); // Updates tab
    await shot(page, 'phone-14-updates');
    await page.mouse.click(315, 752); // About tab
    await shot(page, 'phone-15-about');
    await page.mouse.wheel(0, 900);
    await shot(page, 'phone-16-about');
    await page.mouse.wheel(0, 900);
    await shot(page, 'phone-17-about');
    await page.mouse.click(45, 752); // Explore tab
    await sleep(600);
    await page.mouse.click(300, 24); // Report
    await shot(page, 'phone-18-report');
    await phone.close();

    // Wide: a laptop window.
    const wide = await browser.newContext({ viewport: { width: 1280, height: 860 }, deviceScaleFactor: 1 });
    const w = await wide.newPage();
    await w.goto(url, { waitUntil: 'load' });
    await sleep(10000);
    await shot(w, 'wide-01-explore');
    await w.mouse.click(400, 460); // A project row
    await shot(w, 'wide-02-project');
    await w.mouse.wheel(0, 900);
    await shot(w, 'wide-03-project');
    await wide.close();
  } finally {
    await browser.close();
  }
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
