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

  test('should navigate to documents page and show upload button @smoke @critical', async ({
    page,
  }) => {
    const projectId = await setupProject(page, token)
    await page.goto(`/projects/${projectId}/documents`)
    await expect(page.getByRole('heading', { name: /documents/i })).toBeVisible()
    await expect(page.getByRole('button', { name: /upload/i })).toBeVisible()
  })

  test('should open upload modal when clicking upload button @regression', async ({
    page,
  }) => {
    const projectId = await setupProject(page, token)
    await page.goto(`/projects/${projectId}/documents`)
    await page.getByRole('button', { name: /upload/i }).click()
    await expect(page.locator('[role="dialog"]')).toBeVisible()
    await expect(page.getByText(/upload document/i)).toBeVisible()
  })

  test('should upload a document via modal @regression', async ({ page }) => {
    const projectId = await setupProject(page, token)
    await page.goto(`/projects/${projectId}/documents`)
    await page.getByRole('button', { name: /upload/i }).click()
    await page.locator('.upload__input').fill(`Doc ${Date.now().toString(36)}`)
    const fileChooserPromise = page.waitForEvent('filechooser')
    await page.locator('.upload__browse').click()
    const fileChooser = await fileChooserPromise
    await fileChooser.setFiles({
      name: 'report.txt',
      mimeType: 'text/plain',
      buffer: Buffer.from('report content'),
    })
    await page.getByRole('dialog').getByRole('button', { name: 'Upload' }).click()
    await expect(page.locator('.document-list__table')).toBeVisible()
  })

  test('should show empty state when no documents exist @regression', async ({
    page,
  }) => {
    const projectId = await setupProject(page, token)
    await page.goto(`/projects/${projectId}/documents`)
    await expect(page.getByText(/no documents/i)).toBeVisible()
  })

  test('should delete a document from the list @regression', async ({
    page,
  }) => {
    const projectId = await setupProject(page, token)
    await page.goto(`/projects/${projectId}/documents`)
    await page.getByRole('button', { name: /upload/i }).click()
    await page.locator('.upload__input').fill(`Doc ${Date.now().toString(36)}`)
    const fileChooserPromise = page.waitForEvent('filechooser')
    await page.locator('.upload__browse').click()
    const fileChooser = await fileChooserPromise
    await fileChooser.setFiles({
      name: 'doc.txt',
      mimeType: 'text/plain',
      buffer: Buffer.from('content'),
    })
    await page.getByRole('dialog').getByRole('button', { name: 'Upload' }).click()
    await expect(page.locator('.document-list__table')).toBeVisible()
    page.once('dialog', (dialog) => dialog.accept())
    await page.locator('.document-list__action--danger', { hasText: /delete/i }).click()
    await expect(page.getByText(/no documents/i)).toBeVisible()
  })
})
