// @ts-check
const { test, expect } = require('@playwright/test');

test.describe('Ralph Wiggum Vibe Codes', () => {
    test('page loads with correct title', async ({ page }) => {
        await page.goto('/');
        await expect(page).toHaveTitle('Ralph Wiggum Vibe Codes');
    });

    test('page shows heading', async ({ page }) => {
        await page.goto('/');
        const heading = page.locator('h1');
        await expect(heading).toHaveText('Ralph Wiggum Vibe Codes');
    });

    test('initial quote text prompts user to click button', async ({ page }) => {
        await page.goto('/');
        const quote = page.locator('#quote');
        await expect(quote).toHaveText('Click the button to get your vibe!');
    });

    test('vibe button is visible', async ({ page }) => {
        await page.goto('/');
        const button = page.locator('#vibe-btn');
        await expect(button).toBeVisible();
        await expect(button).toHaveText('Get Vibe');
    });

    test('clicking vibe button updates the quote', async ({ page }) => {
        await page.goto('/');
        const button = page.locator('#vibe-btn');
        const quote = page.locator('#quote');

        await button.click();
        const newQuote = await quote.textContent();
        expect(newQuote).not.toBe('Click the button to get your vibe!');
        expect(newQuote?.length).toBeGreaterThan(0);
    });

    test('clicking vibe button displays a vibe code', async ({ page }) => {
        await page.goto('/');
        const button = page.locator('#vibe-btn');
        const vibeCode = page.locator('#vibe-code');

        await button.click();
        const code = await vibeCode.textContent();
        expect(code).toMatch(/^VIBE-\d{3}:/);
    });
});
