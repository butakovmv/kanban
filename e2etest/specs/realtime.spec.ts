import { test, expect } from '../fixtures'
import {
  loginAsNewUser,
  createProjectViaUi,
  createBoardViaApi,
  createColumnViaApi,
} from '../helpers'

test.describe('Realtime SSE sync', () => {
  test('should sync task creation across two browser contexts @smoke @critical', async ({
    browser,
  }) => {
    const ctx1 = await browser.newContext()
    const ctx2 = await browser.newContext()
    const page1 = await ctx1.newPage()
    const page2 = await ctx2.newPage()

    const creds1 = await loginAsNewUser(page1, 'rt1')
    const token1 = creds1.token

    const projectId = await createProjectViaUi(
      page1,
      `RTProj ${Date.now().toString(36)}`,
    )
    const { boardId } = await createBoardViaApi(
      page1,
      projectId,
      'RT Board',
      token1,
    )
    const colId = await createColumnViaApi(page1, boardId, 'To Do', token1)
    await createColumnViaApi(page1, boardId, 'Done', token1)

    const creds2 = await loginAsNewUser(page2, 'rt2')
    const token2 = creds2.token

    await page2.goto(`/boards/${boardId}`)
    await page2.waitForSelector('.column')
    await page1.goto(`/boards/${boardId}`)
    await page1.waitForSelector('.column')

    const taskTitle = `Realtime Task ${Date.now().toString(36)}`
    await page1.locator('.column__add').first().click()
    await expect(page1.locator('[role="dialog"]')).toBeVisible()
    await page1.locator('.modal__input').first().fill(taskTitle)
    await page1.getByRole('button', { name: 'Create' }).click()
    await expect(page1.getByText(taskTitle)).toBeVisible()

    try {
      await page2.waitForSelector(`text="${taskTitle}"`, { timeout: 10000 })
      await expect(page2.getByText(taskTitle)).toBeVisible()
    } catch {
      await page2.waitForTimeout(2000)
      const page2Task = page2.locator('.task-card').filter({ hasText: taskTitle })
      const count = await page2Task.count()
      expect(count).toBeGreaterThan(0)
    }

    await ctx1.close()
    await ctx2.close()
  })
})
