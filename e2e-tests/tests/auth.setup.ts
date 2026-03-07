import { type Page } from '@playwright/test'

export async function loginViaApi(page: Page, username = 'admin', password = 'admin123') {
  // Navigate to the app first to establish the origin (localStorage requires same-origin)
  await page.goto('/login')
  await page.waitForSelector('[data-testid="login-card"]')

  const response = await page.request.post('/api/auth/login', {
    data: { username, password },
  })
  const data = await response.json()

  await page.evaluate((tokens: { refreshToken: string }) => {
    localStorage.setItem('refreshToken', tokens.refreshToken)
  }, data)

  await page.goto('/dashboard')
  await page.waitForSelector('[data-testid="dashboard-username"]')

  return data
}
