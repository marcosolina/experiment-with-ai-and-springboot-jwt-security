import { test, expect } from '@playwright/test'

test.describe('Login', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login')
  })

  test('should display login form', async ({ page }) => {
    await expect(page.locator('[data-testid="login-card"]')).toBeVisible()
    await expect(page.locator('[data-testid="login-username"]')).toBeVisible()
    await expect(page.locator('[data-testid="login-password"]')).toBeVisible()
    await expect(page.locator('[data-testid="login-submit"]')).toBeVisible()
  })

  test('should login successfully with valid credentials', async ({ page }) => {
    await page.locator('[data-testid="login-username"]').fill('admin')
    await page.locator('[data-testid="login-password"]').fill('admin123')
    await page.locator('[data-testid="login-submit"]').click()

    await expect(page).toHaveURL(/\/dashboard/)
    await expect(page.locator('[data-testid="dashboard-username"]')).toHaveText('admin')
  })

  test('should show error with invalid credentials', async ({ page }) => {
    await page.locator('[data-testid="login-username"]').fill('wrong')
    await page.locator('[data-testid="login-password"]').fill('wrong')
    await page.locator('[data-testid="login-submit"]').click()

    await expect(page.locator('[data-testid="login-error"]')).toBeVisible()
  })
})
