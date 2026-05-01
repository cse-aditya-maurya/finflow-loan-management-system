import { test, expect } from '@playwright/test';

test.describe('User Authentication Flow', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test('should navigate to login page', async ({ page }) => {
    await page.click('text=Sign In');
    await expect(page).toHaveURL('/login');
    await expect(page.locator('h1')).toContainText('Welcome back');
  });

  test('should navigate to signup page', async ({ page }) => {
    await page.click('text=Apply Now');
    await expect(page).toHaveURL('/signup');
    await expect(page.locator('h1')).toContainText('Create your account');
  });

  test('should show validation errors on empty login', async ({ page }) => {
    await page.goto('/login');
    await page.click('button:has-text("Sign In")');
    await expect(page.locator('text=Please fill in all fields')).toBeVisible();
  });

  test('should show validation errors on empty signup', async ({ page }) => {
    await page.goto('/signup');
    await page.click('button:has-text("Create Account")');
    await expect(page.locator('text=Full name is required')).toBeVisible();
  });

  test('should validate email format', async ({ page }) => {
    await page.goto('/signup');
    await page.fill('input[name="name"]', 'John Doe');
    await page.fill('input[name="email"]', 'invalid-email');
    await page.fill('input[name="password"]', 'password123');
    await page.fill('input[name="confirmPassword"]', 'password123');
    await page.click('button:has-text("Create Account")');
    await expect(page.locator('text=Enter a valid email')).toBeVisible();
  });

  test('should validate password match', async ({ page }) => {
    await page.goto('/signup');
    await page.fill('input[name="name"]', 'John Doe');
    await page.fill('input[name="email"]', 'john@example.com');
    await page.fill('input[name="password"]', 'password123');
    await page.fill('input[name="confirmPassword"]', 'different123');
    await page.click('button:has-text("Create Account")');
    await expect(page.locator('text=Passwords do not match')).toBeVisible();
  });

  test('should toggle password visibility', async ({ page }) => {
    await page.goto('/login');
    const passwordInput = page.locator('input[name="password"]');
    
    await expect(passwordInput).toHaveAttribute('type', 'password');
    
    await page.click('button[type="button"]');
    await expect(passwordInput).toHaveAttribute('type', 'text');
  });

  test('should show password strength indicator', async ({ page }) => {
    await page.goto('/signup');
    const passwordInput = page.locator('input[name="password"]');
    
    await passwordInput.fill('12345');
    await expect(page.locator('text=Weak')).toBeVisible();
    
    await passwordInput.fill('12345678');
    await expect(page.locator('text=Fair')).toBeVisible();
    
    await passwordInput.fill('1234567890');
    await expect(page.locator('text=Strong')).toBeVisible();
  });

  test('should have forgot password link', async ({ page }) => {
    await page.goto('/login');
    await expect(page.locator('text=Forgot password?')).toBeVisible();
  });

  test('should navigate between login and signup', async ({ page }) => {
    await page.goto('/login');
    await page.click('text=Create one for free');
    await expect(page).toHaveURL('/signup');
    
    await page.click('text=Sign in');
    await expect(page).toHaveURL('/login');
  });
});
