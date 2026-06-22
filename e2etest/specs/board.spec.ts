import { test, expect } from '../fixtures'
import {
  loginAsNewUser,
  createProjectViaUi,
  createBoardViaApi,
} from '../helpers'

test.describe('Board operations', () => {
  let token: string

  test.beforeEach(async ({ page }) => {
    const creds = await loginAsNewUser(page, 'brd')
    token = creds.token
  })

  test('should view board page with columns @smoke @critical', async ({ page }) => {
    const projectId = await createProjectViaUi(page, `BoardProj ${Date.now().toString(36)}`)
    const { boardId } = await createBoardViaApi(page, projectId, 'Test Board', token)
    await page.goto(`/boards/${boardId}`)
    await expect(page.locator('.board__title')).toBeVisible()
    await expect(page.getByText('Test Board')).toBeVisible()
  })

  test('should show add column button on board page @regression', async ({ page }) => {
    const projectId = await createProjectViaUi(page, `BoardProj ${Date.now().toString(36)}`)
    const { boardId } = await createBoardViaApi(page, projectId, 'Board', token)
    await page.goto(`/boards/${boardId}`)
    await expect(page.getByRole('button', { name: /add column/i })).toBeVisible()
  })

  test('should display swimlanes toggle @regression', async ({ page }) => {
    const projectId = await createProjectViaUi(page, `BoardProj ${Date.now().toString(36)}`)
    const { boardId } = await createBoardViaApi(page, projectId, 'Board', token)
    await page.goto(`/boards/${boardId}`)
    const toggle = page.locator('.board__swimlanes-toggle')
    await expect(toggle).toBeVisible()
    await toggle.click()
    await expect(toggle).toHaveAttribute('aria-pressed', 'true')
    await toggle.click()
    await expect(toggle).toHaveAttribute('aria-pressed', 'false')
  })
})
