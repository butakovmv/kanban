import { test, expect } from '../fixtures'
import { loginAsNewUser } from '../helpers'

test.describe('Project CRUD', () => {
  let email: string

  test.beforeEach(async ({ page }) => {
    const creds = await loginAsNewUser(page, 'proj')
    email = creds.email
  })

  test('should create a project and display it in the list @smoke @critical', async ({ page }) => {
    const name = `Project ${Date.now().toString(36)}`
    await page.goto('/projects')
    await page.getByRole('button', { name: /new project/i }).click()
    await page.getByRole('textbox', { name: /name/i }).fill(name)
    await page.getByRole('button', { name: 'Create' }).click()
    await expect(page.locator('.project-list__item')).toBeVisible()
    await expect(page.getByText(name)).toBeVisible()
  })

  test('should open project settings and update name @regression', async ({ page }) => {
    const name = `Project ${Date.now().toString(36)}`
    const updatedName = name + ' (updated)'
    await page.goto('/projects')
    await page.getByRole('button', { name: /new project/i }).click()
    await page.getByRole('textbox', { name: /name/i }).fill(name)
    await page.getByRole('button', { name: 'Create' }).click()
    await page.waitForSelector('.project-list__item')
    await page.locator('.project-list__link').first().click()
    await page.waitForURL(/\/projects\//)
    await expect(page.getByText('Project settings')).toBeVisible()
    await page.getByRole('textbox', { name: /name/i }).fill(updatedName)
    await page.getByRole('button', { name: 'Save' }).click()
    await expect(page.locator('input').filter({ hasValue: updatedName })).toBeVisible()
  })

  test('should delete a project @regression', async ({ page }) => {
    const name = `Project ${Date.now().toString(36)}`
    await page.goto('/projects')
    await page.getByRole('button', { name: /new project/i }).click()
    await page.getByRole('textbox', { name: /name/i }).fill(name)
    await page.getByRole('button', { name: 'Create' }).click()
    await page.waitForSelector('.project-list__item')
    await page.locator('.project-list__link').first().click()
    await page.waitForURL(/\/projects\//)
    page.once('dialog', (dialog) => dialog.accept())
    await page.getByRole('button', { name: /delete project/i }).click()
    await expect(page).toHaveURL(/\/projects$|\/projects\?/)
  })
})
