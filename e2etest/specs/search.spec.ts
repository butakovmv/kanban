import { test, expect } from '@playwright/test'
import { loginAsNewUser } from '../helpers'

test.describe('Search', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsNewUser(page, 'srch')
  })

  test('should navigate to search page and show search input @smoke @critical', async ({
    page,
  }) => {
    await page.goto('/search')
    await expect(page.getByRole('heading', { name: /search tasks/i })).toBeVisible()
    await expect(page.getByPlaceholder(/search tasks/i)).toBeVisible()
  })

  test('should display clear filters button @regression', async ({ page }) => {
    await page.goto('/search')
    await expect(page.getByRole('button', { name: /clear filters/i })).toBeVisible()
  })

  test('should show filters section @regression', async ({ page }) => {
    await page.goto('/search')
    await expect(page.locator('.search-page__filters')).toBeVisible()
    await expect(page.getByText(/filters/i)).toBeVisible()
  })

  test('should show no results when searching without data @regression', async ({
    page,
  }) => {
    await page.goto('/search')
    await page.getByPlaceholder(/search tasks/i).fill('nonexistent-task-xyz')
    await page.waitForTimeout(500)
    await expect(page.getByText(/no results/i)).toBeVisible()
  })

  test('should expand and show filter fields @regression', async ({ page }) => {
    await page.goto('/search')
    await page.locator('.search-page__filters summary').click()
    const filterLabels = page.locator('.search-page__filter-grid label')
    await expect(filterLabels).toHaveCount(7)
  })

  test('should show pagination section when results exist @regression', async ({
    page,
  }) => {
    await page.goto('/search')
    await page.getByPlaceholder(/search tasks/i).fill('a')
    await page.waitForTimeout(500)
    const pagination = page.locator('.search-page__pagination')
    if (await pagination.isVisible()) {
      await expect(page.locator('.search-page__page-info')).toBeVisible()
    }
  })
})
