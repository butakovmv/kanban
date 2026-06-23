import { test, expect } from '../fixtures'
import { loginAsNewUser, createProjectViaUi } from '../helpers'

test.describe('Document operations', () => {
  let token: string

  test.beforeEach(async ({ page }) => {
    const creds = await loginAsNewUser(page, 'doc')
    token = creds.token
  })

  async function setupProject(page: any, token: string): Promise<string> {
    return createProjectViaUi(page, `DocProj ${Date.now().toString(36)}`)
  }

  test('should navigate to documents page and show create button @smoke @critical', async ({ page }) => {
    const projectId = await setupProject(page, token)
    await page.goto(`/projects/${projectId}/documents`)
    await expect(page.getByRole('heading', { name: /documents/i })).toBeVisible()
    await expect(page.getByRole('button', { name: /new/i })).toBeVisible()
  })

  test('should open create modal when clicking new button @regression', async ({ page }) => {
    const projectId = await setupProject(page, token)
    await page.goto(`/projects/${projectId}/documents`)
    await page.getByRole('button', { name: /new/i }).click()
    await expect(page.locator('[role="dialog"]')).toBeVisible()
    await expect(page.getByText(/create document/i)).toBeVisible()
  })

  test('should create a document via modal @regression', async ({ page }) => {
    const projectId = await setupProject(page, token)
    await page.goto(`/projects/${projectId}/documents`)
    await page.getByRole('button', { name: /new/i }).click()

    const docId = Date.now().toString(36)
    await page.getByRole('textbox', { name: /path/i }).fill(`docs/report-${docId}`)
    await page.getByRole('textbox', { name: /title/i }).fill(`Report ${docId}`)
    await page.getByRole('textbox', { name: /content/i }).fill(`# Report ${docId}\n\nSome markdown content.`)

    await page.getByRole('dialog').getByRole('button', { name: 'Create' }).click()
    await expect(page.locator('.document-list__doc-name')).toHaveText(`Report ${docId}`)
  })

  test('should show empty state when no documents exist @regression', async ({ page }) => {
    const projectId = await setupProject(page, token)
    await page.goto(`/projects/${projectId}/documents`)
    await expect(page.getByText(/no documents/i)).toBeVisible()
  })

  test('should delete a document from the list @regression', async ({ page }) => {
    const projectId = await setupProject(page, token)
    await page.goto(`/projects/${projectId}/documents`)
    await page.getByRole('button', { name: /new/i }).click()

    const docId = Date.now().toString(36)
    await page.getByRole('textbox', { name: /path/i }).fill(`docs/report-${docId}`)
    await page.getByRole('textbox', { name: /title/i }).fill(`Report ${docId}`)
    await page.getByRole('textbox', { name: /content/i }).fill(`# Report ${docId}`)

    await page.getByRole('dialog').getByRole('button', { name: 'Create' }).click()
    await expect(page.locator('.document-list__doc-name')).toBeVisible()

    page.once('dialog', (dialog) => dialog.accept())
    await page.locator('.document-list__action--danger', { hasText: /delete/i }).click()
    await expect(page.getByText(/no documents/i)).toBeVisible()
  })
})
