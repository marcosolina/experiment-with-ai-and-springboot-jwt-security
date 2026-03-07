import { test, expect } from '@playwright/test'
import { loginViaApi } from './auth.setup'

test.describe('Logout', () => {
  test('should logout and redirect to login', async ({ page }) => {
    await loginViaApi(page)

    await page.locator('[data-testid="navbar-logout"]').click()
    await expect(page).toHaveURL(/\/login/)
  })

  test('should not access dashboard after logout', async ({ page }) => {
    await loginViaApi(page)

    await page.locator('[data-testid="navbar-logout"]').click()
    await expect(page).toHaveURL(/\/login/)

    await page.goto('/dashboard')
    await expect(page).toHaveURL(/\/login/)
  })
})
