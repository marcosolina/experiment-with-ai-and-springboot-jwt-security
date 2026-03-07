import { test, expect } from '@playwright/test'
import { loginViaApi } from './auth.setup'

test.describe('Messages', () => {
  test.beforeEach(async ({ page }) => {
    await loginViaApi(page)
    await page.locator('[data-testid="nav-messages"]').click()
    await page.waitForSelector('[data-testid="messages-list"]')
  })

  test('should display messages list', async ({ page }) => {
    await expect(page.locator('[data-testid="messages-list"]')).toBeVisible()
    const rows = page.locator('[data-testid^="message-item-"]')
    await expect(rows.first()).toBeVisible()
  })

  test('should create a new message', async ({ page }) => {
    const messageText = `Test message ${Date.now()}`
    await page.locator('[data-testid="message-input"]').fill(messageText)
    await page.locator('[data-testid="message-submit"]').click()

    await expect(page.locator(`text=${messageText}`)).toBeVisible()
  })

  test('should delete a message', async ({ page }) => {
    const messageText = `Delete me ${Date.now()}`
    await page.locator('[data-testid="message-input"]').fill(messageText)
    await page.locator('[data-testid="message-submit"]').click()
    await expect(page.locator(`text=${messageText}`)).toBeVisible()

    const row = page.locator(`tr:has-text("${messageText}")`)
    await row.locator('[data-testid^="message-delete-"]').click()

    await expect(page.locator(`text=${messageText}`)).not.toBeVisible()
  })

  test('should show health status', async ({ page }) => {
    await expect(page.locator('[data-testid="messages-health"]')).toBeVisible()
  })
})
