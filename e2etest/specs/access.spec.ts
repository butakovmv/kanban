import { test, expect } from '../fixtures'
import { loginAsNewUser } from '../helpers'

test.describe('Access control', () => {
  let email: string

  test.beforeEach(async ({ page }) => {
    const creds = await loginAsNewUser(page, 'ac')
    email = creds.email
  })

  test('should navigate to access control page and show create button @smoke @critical', async ({
    page,
  }) => {
    await page.goto('/access')
    await expect(page.getByRole('heading', { name: /управление доступом/i })).toBeVisible()
    await expect(page.getByRole('button', { name: /создать группу/i })).toBeVisible()
  })

  test('should create a group and display in sidebar @regression', async ({
    page,
  }) => {
    await page.goto('/access')
    await page.getByRole('button', { name: /создать группу/i }).click()
    await page.locator('.access-control__form input[type="text"]').first().fill(`Group ${Date.now().toString(36)}`)
    await page.locator('.access-control__form button[type="submit"]').click()
    await expect(page.locator('.access-control__group-item')).toBeVisible()
  })

  test('should select a group and view its details @regression', async ({
    page,
  }) => {
    await page.goto('/access')
    await page.getByRole('button', { name: /создать группу/i }).click()
    await page.locator('.access-control__form input[type="text"]').first().fill(`Group ${Date.now().toString(36)}`)
    await page.locator('.access-control__form button[type="submit"]').click()
    await page.waitForSelector('.access-control__group-item')
    await page.locator('.access-control__group-btn').first().click()
    await expect(page.locator('.access-control__main')).toBeVisible()
  })

  test('should add a member to a group @regression', async ({ page }) => {
    await page.goto('/access')
    await page.getByRole('button', { name: /создать группу/i }).click()
    await page.locator('.access-control__form input[type="text"]').first().fill(`Group ${Date.now().toString(36)}`)
    await page.locator('.access-control__form button[type="submit"]').click()
    await page.waitForSelector('.access-control__group-item')
    await page.locator('.access-control__group-btn').first().click()
    await page.waitForSelector('.access-control__main')
    await page.locator('.access-control__add-member input').fill('test-user-id')
    await page.locator('.access-control__add-member button').click()
    await expect(page.locator('.access-control__member-item')).toBeVisible()
  })

  test('should switch to permissions tab @regression', async ({ page }) => {
    await page.goto('/access')
    await page.getByRole('button', { name: /создать группу/i }).click()
    await page.locator('.access-control__form input[type="text"]').first().fill(`Group ${Date.now().toString(36)}`)
    await page.locator('.access-control__form button[type="submit"]').click()
    await page.waitForSelector('.access-control__group-item')
    await page.locator('.access-control__group-btn').first().click()
    await page.waitForSelector('.access-control__main')
    await page.locator('.access-control__tab', { hasText: /разрешения/i }).click()
    await expect(page.getByText(/разрешения группы/i)).toBeVisible()
  })

  test('should delete a group with confirmation @regression', async ({
    page,
  }) => {
    await page.goto('/access')
    await page.getByRole('button', { name: /создать группу/i }).click()
    await page.locator('.access-control__form input[type="text"]').first().fill(`Group ${Date.now().toString(36)}`)
    await page.locator('.access-control__form button[type="submit"]').click()
    await page.waitForSelector('.access-control__group-item')
    const deleteBtn = page.locator('.access-control__group-delete').first()
    await deleteBtn.click()
    await expect(page.locator('.access-control__confirm-dialog')).toBeVisible()
    await page.locator('.access-control__confirm-dialog button').first().click()
    await expect(page.locator('.access-control__group-item')).not.toBeVisible()
  })
})
