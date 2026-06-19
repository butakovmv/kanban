import { test, expect } from '@playwright/test'

test.describe('App', () => {
  test('homepage loads', { tag: '@smoke' }, async ({ page }) => {
    await page.goto('/')
    await expect(page).toHaveTitle(/Kanban/)
  })
})
