import { chromium } from 'playwright';

(async () => {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext();

  let newPageResolve;
  const newPagePromise = new Promise((res) => { newPageResolve = res; });
  context.on('page', (p) => newPageResolve(p));

  const page = await context.newPage();
  await page.goto('http://localhost:5173/');

  await page.fill('input[placeholder="https://example.com"]', 'https://example.com');
  await page.fill('textarea', 'Extract headings and paragraphs');

  await page.click('button:has-text("Extract & Download")');

  let newPage = null;
  try {
    newPage = await Promise.race([
      newPagePromise,
      new Promise((_, rej) => setTimeout(() => rej(new Error('timeout')), 5000))
    ]);
  } catch (e) {
    console.error('No new page opened within timeout.');
  }

  if (newPage) {
    await newPage.waitForLoadState('domcontentloaded');
    const content = await newPage.content();
    console.log('New page content snapshot:\n', content.slice(0, 1000));
  }

  await browser.close();
})();
