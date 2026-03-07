import { test, expect } from '@playwright/test'
import { loginViaApi } from './auth.setup'

test.describe('Dashboard', () => {
  test('should display user info after login', async ({ page }) => {
    await loginViaApi(page)

    await expect(page.locator('[data-testid="dashboard-username"]')).toHaveText('admin')
    await expect(page.locator('[data-testid="dashboard-roles"]')).toBeVisible()
  })

  test('should redirect to login when not authenticated', async ({ page }) => {
    await page.goto('/dashboard')
    await expect(page).toHaveURL(/\/login/)
  })

  test('should navigate to messages page', async ({ page }) => {
    await loginViaApi(page)

    await page.locator('[data-testid="dashboard-go-messages"]').click()
    await expect(page).toHaveURL(/\/messages/)
  })
})
