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

function watch(page) {
  page.on('pageerror', (e) => console.log('page error:', e.message));
  page.on('console', (m) => { if (m.type() === 'error' || m.type() === 'warning') console.log('console ' + m.type() + ':', m.text()); });
}

// Says whether the Compose canvas exists and WebGL2 is available, so a blank capture can be explained.
async function diagnose(page) {
  const info = await page.evaluate(() => {
    const canvas = document.querySelector('canvas');
    const probe = document.createElement('canvas');
    return {
      canvas: canvas ? canvas.width + 'x' + canvas.height : 'none',
      webgl2: !!probe.getContext('webgl2'),
      language: navigator.language,
      title: document.title,
    };
  });
  console.log('diagnostics:', JSON.stringify(info));
}

async function shot(page, name) {
  await sleep(900);
  await page.screenshot({ path: path.join(out, name + '.png') });
  console.log('saved', name);
}

(async () => {
  // Full Chromium in new headless mode, with software WebGL: the Compose canvas needs WebGL2, and the
  // headless shell disables SwiftShader unless asked.
  const launch = {
    args: ['--no-sandbox', '--enable-unsafe-swiftshader', '--use-gl=angle', '--use-angle=swiftshader', '--ignore-gpu-blocklist'],
  };
  if (process.env.CHROMIUM_PATH) launch.executablePath = process.env.CHROMIUM_PATH;
  else launch.channel = 'chromium';
  const browser = await chromium.launch(launch);
  try {
    // Phone: a 360 x 780 window, the most common Android size class.
    // An explicit locale: without one the Kotlin/Wasm runtime fails on "Incorrect locale information".
    const phone = await browser.newContext({ viewport: { width: 360, height: 780 }, deviceScaleFactor: 2, hasTouch: true, locale: 'en-PH' });
    const page = await phone.newPage();
    watch(page);
    await page.goto(url, { waitUntil: 'load' });
    await sleep(12000); // Wasm boot and font loading.
    await diagnose(page);
    await shot(page, 'phone-01-explore');
    await page.mouse.wheel(0, 520);
    await shot(page, 'phone-02-explore-cards');
    await page.mouse.wheel(0, 900);
    await shot(page, 'phone-03-explore-more');
    await page.mouse.wheel(0, -5000);
    await sleep(600);
    await page.mouse.click(320, 323); // More filters, beside the search box
    await shot(page, 'phone-04-filters');
    await page.mouse.click(28, 28); // Close
    await sleep(800);
    await page.mouse.wheel(0, 380);
    await sleep(500);
    await page.mouse.click(150, 330); // A project card
    await shot(page, 'phone-05-project');
    // CI renders in software, where backdrop blur makes each frame slow, so give the page and the sheet
    // several seconds to settle before acting on them.
    await sleep(3000);
    await page.mouse.click(252, 500); // The source badge beside reported progress
    await sleep(4000);
    await shot(page, 'phone-06-source-sheet');
    await page.mouse.click(180, 120); // The scrim closes the sheet
    await sleep(3000);
    await page.mouse.wheel(0, 650);
    await shot(page, 'phone-07-project');
    for (let i = 8; i <= 12; i++) {
      await page.mouse.wheel(0, 700);
      await shot(page, 'phone-' + String(i).padStart(2, '0') + '-project');
    }
    await page.mouse.wheel(0, -9000);
    await sleep(600);
    await page.mouse.click(28, 28); // Back
    await sleep(800);
    await page.mouse.click(141, 736); // Watchlist tab
    await shot(page, 'phone-13-watchlist');
    await page.mouse.click(219, 736); // Updates tab
    await shot(page, 'phone-14-updates');
    await page.mouse.click(296, 736); // About tab
    await shot(page, 'phone-15-about');
    await page.mouse.wheel(0, 800);
    await shot(page, 'phone-16-about');
    await page.mouse.wheel(0, 800);
    await shot(page, 'phone-17-about');
    await page.mouse.click(63, 736); // Explore tab
    await sleep(600);
    await page.mouse.wheel(0, -5000);
    await sleep(400);
    await page.mouse.click(300, 28); // Report
    await shot(page, 'phone-18-report');
    await phone.close();

    // Wide: a laptop window.
    const wide = await browser.newContext({ viewport: { width: 1280, height: 860 }, deviceScaleFactor: 1, locale: 'en-PH' });
    const w = await wide.newPage();
    watch(w);
    await w.goto(url, { waitUntil: 'load' });
    await sleep(12000);
    await diagnose(w);
    await shot(w, 'wide-01-explore');
    await w.mouse.click(300, 560); // A project card
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
