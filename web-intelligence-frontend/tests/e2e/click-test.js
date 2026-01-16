const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext();

  // Capture new pages (the app opens results in a new window)
  let newPagePromise = null;
  context.on('page', (p) => {
    if (newPagePromise) newPagePromise.resolve(p);
  });

  async function waitForNewPage() {
    return new Promise((resolve) => { newPagePromise = { resolve }; });
  }

  const page = await context.newPage();
  await page.goto('http://localhost:5173/');

  // Fill inputs and click button
  await page.fill('input[placeholder="https://example.com"]', 'https://example.com');
  await page.fill('textarea', 'Extract headings and paragraphs');

  // Click and wait for either a download or new page
  const newPagePromiseObj = waitForNewPage();
  await page.click('button:has-text("Extract & Download")');

  let newPage = null;
  try {
    newPage = await Promise.race([
      newPagePromiseObj,
      new Promise((_, rej) => setTimeout(() => rej(new Error('timeout')), 5000))
    ]);
  } catch (e) {
    console.error('No new page opened; attempting to fetch network response via backend.');
  }

  if (newPage) {
    await newPage.waitForLoadState('domcontentloaded');
    const content = await newPage.content();
    console.log('New page content snapshot:\n', content.slice(0, 1000));
  }

  await browser.close();
})();
