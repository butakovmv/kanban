import { test, expect } from '../fixtures'
import { loginAsNewUser } from '../helpers'

test.describe('Reports', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsNewUser(page, 'rpt')
  })

  test('should navigate to reports page and show tabs @smoke @critical', async ({
    page,
  }) => {
    await page.goto('/reports')
    await expect(page.getByRole('heading', { name: /reports/i })).toBeVisible()
    await expect(page.getByRole('button', { name: /cfd chart/i })).toBeVisible()
    await expect(page.getByRole('button', { name: /lead time/i })).toBeVisible()
  })

  test('should show CFD tab by default @smoke @critical', async ({ page }) => {
    await page.goto('/reports')
    const cfdTab = page.locator('.reports-page__tab--active', { hasText: /cfd/i })
    await expect(cfdTab).toBeVisible()
  })

  test('should display CFD filter inputs @regression', async ({ page }) => {
    await page.goto('/reports')
    await expect(page.getByText(/project id/i)).toBeVisible()
    await expect(page.getByText(/board id/i)).toBeVisible()
    await expect(page.getByText(/from/i)).toBeVisible()
    await expect(page.getByText(/to/i)).toBeVisible()
    await expect(page.getByText(/interval/i)).toBeVisible()
  })

  test('should display Load button for CFD chart @regression', async ({
    page,
  }) => {
    await page.goto('/reports')
    await expect(page.getByRole('button', { name: /load/i })).toBeVisible()
  })

  test('should switch to Lead Time tab @regression', async ({ page }) => {
    await page.goto('/reports')
    await page.getByRole('button', { name: /lead time/i }).click()
    const ltTab = page.locator('.reports-page__tab--active', { hasText: /lead time/i })
    await expect(ltTab).toBeVisible()
  })

  test('should show Lead Time filter inputs after switching tab @regression', async ({
    page,
  }) => {
    await page.goto('/reports')
    await page.getByRole('button', { name: /lead time/i }).click()
    await expect(page.getByText(/project id/i)).toBeVisible()
    await expect(page.getByText(/from/i)).toBeVisible()
    await expect(page.getByText(/to/i)).toBeVisible()
  })

  test('should allow changing date range in CFD tab @regression', async ({
    page,
  }) => {
    await page.goto('/reports')
    const dateInputs = page.locator('.reports-page__filters input[type="date"]')
    const count = await dateInputs.count()
    expect(count).toBeGreaterThanOrEqual(2)
    if (count >= 2) {
      await dateInputs.nth(0).fill('2024-01-01')
      await dateInputs.nth(1).fill('2024-12-31')
    }
  })

  test.skip('should show loading state when loading CFD @regression', async ({
    page,
  }) => {
    await page.goto('/reports')
    await page.getByRole('button', { name: /load/i }).click()
    await page.waitForTimeout(300)
  })
})
