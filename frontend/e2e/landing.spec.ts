import { test, expect } from '@playwright/test';

test.describe('Landing Page', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test('should display hero section', async ({ page }) => {
    await expect(page.locator('h1')).toContainText('Smart Loans');
    await expect(page.locator('text=Faster Decisions')).toBeVisible();
  });

  test('should display loan types', async ({ page }) => {
    await expect(page.locator('text=Home Loan')).toBeVisible();
    await expect(page.locator('text=Education')).toBeVisible();
    await expect(page.locator('text=Business')).toBeVisible();
    await expect(page.locator('text=Vehicle')).toBeVisible();
    await expect(page.locator('text=Personal')).toBeVisible();
  });

  test('should display features section', async ({ page }) => {
    await expect(page.locator('text=Why FinFlow?')).toBeVisible();
    await expect(page.locator('text=Instant Decisions')).toBeVisible();
    await expect(page.locator('text=Bank-Grade Security')).toBeVisible();
    await expect(page.locator('text=Real-time Tracking')).toBeVisible();
  });

  test('should display how it works section', async ({ page }) => {
    await expect(page.locator('text=How It Works')).toBeVisible();
    await expect(page.locator('text=Create Account')).toBeVisible();
    await expect(page.locator('text=Apply Online')).toBeVisible();
    await expect(page.locator('text=Get Approved')).toBeVisible();
  });

  test('should have CTA buttons', async ({ page }) => {
    const applyButton = page.locator('text=Apply Now — It\'s Free').first();
    await expect(applyButton).toBeVisible();
    
    const signInButton = page.locator('text=Sign In').first();
    await expect(signInButton).toBeVisible();
  });

  test('should navigate to signup on CTA click', async ({ page }) => {
    await page.click('text=Apply Now — It\'s Free');
    await expect(page).toHaveURL('/signup');
  });

  test('should navigate to login on sign in click', async ({ page }) => {
    await page.click('text=Sign In');
    await expect(page).toHaveURL('/login');
  });

  test('should display trust indicators', async ({ page }) => {
    await expect(page.locator('text=No hidden fees')).toBeVisible();
    await expect(page.locator('text=100% Digital')).toBeVisible();
    await expect(page.locator('text=Instant OTP verification')).toBeVisible();
  });

  test('should display footer', async ({ page }) => {
    const currentYear = new Date().getFullYear();
    await expect(page.locator(`text=© ${currentYear} FinFlow`)).toBeVisible();
  });

  test('should be responsive', async ({ page }) => {
    // Test mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });
    await expect(page.locator('h1')).toBeVisible();
    
    // Test tablet viewport
    await page.setViewportSize({ width: 768, height: 1024 });
    await expect(page.locator('h1')).toBeVisible();
    
    // Test desktop viewport
    await page.setViewportSize({ width: 1920, height: 1080 });
    await expect(page.locator('h1')).toBeVisible();
  });
});
