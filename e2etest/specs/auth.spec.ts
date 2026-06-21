import { test, expect } from '../fixtures'

test.describe('Auth flow', () => {
  test('should navigate from login to register page', async ({ page }) => {
    await page.goto('/login')
    await page.getByRole('link', { name: 'Register' }).click()
    await expect(page).toHaveURL(/\/register/)
    await expect(page.getByRole('heading', { name: 'Register' })).toBeVisible()
  })

  test('should navigate from register to login page', async ({ page }) => {
    await page.goto('/register')
    await page.getByRole('link', { name: 'Login' }).click()
    await expect(page).toHaveURL(/\/login/)
    await expect(page.getByRole('heading', { name: 'Login' })).toBeVisible()
  })

  test('should redirect root to login', async ({ page }) => {
    await page.goto('/')
    await expect(page).toHaveURL(/\/login/)
    await expect(page.getByRole('heading', { name: 'Login' })).toBeVisible()
  })

  test.describe('registration', () => {
    test('should register and redirect to login', async ({ page }) => {
      await page.goto('/register')
      await page.getByRole('textbox', { name: /name/i }).fill('Test User')
      await page.getByRole('textbox', { name: /email/i }).fill(`e2e-${Date.now()}@kanban.test`)
      await page.getByRole('textbox', { name: /password/i }).fill('test-password-123')
      await page.getByRole('button', { name: 'Register' }).click()
      await expect(page).toHaveURL(/\/login/)
    })
  })

  test.describe('login', () => {
    test('should login and redirect to projects', async ({ page }) => {
      await page.goto('/login')
      await page.getByRole('textbox', { name: /email/i }).fill('user@kanban.test')
      await page.getByRole('textbox', { name: /password/i }).fill('password')
      await page.getByRole('button', { name: 'Login' }).click()
      await expect(page).toHaveURL(/\/projects/)
    })
  })
})
