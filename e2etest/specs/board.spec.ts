import { test, expect } from '@playwright/test'
import {
  loginAsNewUser,
  createProjectViaUi,
  createBoardViaApi,
  createColumnViaApi,
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
    await createColumnViaApi(page, boardId, 'To Do', token)
    await createColumnViaApi(page, boardId, 'In Progress', token)
    await createColumnViaApi(page, boardId, 'Done', token)
    await page.goto(`/boards/${boardId}`)
    await expect(page.locator('.board__title')).toBeVisible()
    await expect(page.getByText('Test Board')).toBeVisible()
    await expect(page.getByText('To Do')).toBeVisible()
    await expect(page.getByText('In Progress')).toBeVisible()
    await expect(page.getByText('Done')).toBeVisible()
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

  test('should show empty state when no columns exist @regression', async ({ page }) => {
    const projectId = await createProjectViaUi(page, `BoardProj ${Date.now().toString(36)}`)
    const { boardId } = await createBoardViaApi(page, projectId, 'Empty Board', token)
    await page.goto(`/boards/${boardId}`)
    await expect(page.getByText(/no columns/i)).toBeVisible()
  })
})
