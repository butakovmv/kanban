import { test, expect } from '../fixtures'
import {
  loginAsNewUser,
  createProjectViaUi,
  createBoardViaApi,
  createColumnViaApi,
  createTaskViaApi,
} from '../helpers'

test.describe('Comment operations', () => {
  let token: string

  test.beforeEach(async ({ page }) => {
    const creds = await loginAsNewUser(page, 'cmt')
    token = creds.token
  })

  async function setupTask(page: any, token: string): Promise<string> {
    const projectId = await createProjectViaUi(
      page,
      `CommentProj ${Date.now().toString(36)}`,
    )
    const { boardId } = await createBoardViaApi(
      page,
      projectId,
      'Comment Board',
      token,
    )
    const colId = await createColumnViaApi(page, boardId, 'To Do', token)
    const taskId = await createTaskViaApi(
      page,
      boardId,
      colId,
      `CommentTask ${Date.now().toString(36)}`,
      token,
    )
    return taskId
  }

  test('should add a comment to a task @smoke @critical', async ({ page }) => {
    const taskId = await setupTask(page, token)
    await page.goto(`/tasks/${taskId}`)
    await page.waitForSelector('.task-detail__card')
    const commentText = `Test comment ${Date.now().toString(36)}`
    await page.locator('.comments__input').fill(commentText)
    await page.getByRole('button', { name: 'Post' }).click()
    await expect(page.getByText(commentText)).toBeVisible()
  })

  test('should edit an existing comment @regression', async ({ page }) => {
    const taskId = await setupTask(page, token)
    const commentText = `Original comment ${Date.now().toString(36)}`
    await page.goto(`/tasks/${taskId}`)
    await page.waitForSelector('.task-detail__card')
    await page.locator('.comments__input').fill(commentText)
    await page.getByRole('button', { name: 'Post' }).click()
    await expect(page.getByText(commentText)).toBeVisible()
    await page.locator('.comments__action', { hasText: 'Edit' }).first().click()
    const updatedText = commentText + ' (edited)'
    await page.locator('.comments__edit-input').fill(updatedText)
    await page.locator('.comments__action--primary', { hasText: 'Save' }).click()
    await expect(page.getByText(updatedText)).toBeVisible()
  })

  test('should delete a comment @regression', async ({ page }) => {
    const taskId = await setupTask(page, token)
    const commentText = `Delete me ${Date.now().toString(36)}`
    await page.goto(`/tasks/${taskId}`)
    await page.waitForSelector('.task-detail__card')
    await page.locator('.comments__input').fill(commentText)
    await page.getByRole('button', { name: 'Post' }).click()
    await expect(page.getByText(commentText)).toBeVisible()
    await page.locator('.comments__action--danger', { hasText: 'Delete' }).click()
    await expect(page.getByText(commentText)).not.toBeVisible()
  })
})
