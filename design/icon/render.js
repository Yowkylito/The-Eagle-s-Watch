// Rasterises one SVG at a given pixel size with headless Chromium.
// Usage: node render.js <file.svg> <out.png> <size>
// Needs the playwright package; set CHROMIUM_PATH to use a Chromium that is already installed.
const { chromium } = require('playwright');
const path = require('path');

(async () => {
  const [file, out, size] = process.argv.slice(2);
  const launch = { args: ['--no-sandbox'] };
  if (process.env.CHROMIUM_PATH) launch.executablePath = process.env.CHROMIUM_PATH;
  const browser = await chromium.launch(launch);
  const context = await browser.newContext({ viewport: { width: 1024, height: 1024 }, deviceScaleFactor: Number(size) / 1024 });
  const page = await context.newPage();
  await page.goto('file://' + path.resolve(file));
  await page.screenshot({ path: out, omitBackground: true });
  await browser.close();
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
