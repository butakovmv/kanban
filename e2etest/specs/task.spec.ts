import { test, expect } from '@playwright/test'
import {
  loginAsNewUser,
  createProjectViaUi,
  createBoardViaApi,
  createColumnViaApi,
} from '../helpers'

test.describe('Task CRUD and operations', () => {
  let token: string

  test.beforeEach(async ({ page }) => {
    const creds = await loginAsNewUser(page, 'task')
    token = creds.token
  })

  async function setupBoard(page: any, token: string) {
    const projectId = await createProjectViaUi(
      page,
      `TaskProj ${Date.now().toString(36)}`,
    )
    const { boardId } = await createBoardViaApi(
      page,
      projectId,
      'Task Board',
      token,
    )
    const col1 = await createColumnViaApi(page, boardId, 'To Do', token)
    const col2 = await createColumnViaApi(page, boardId, 'Done', token)
    return { projectId, boardId, col1, col2 }
  }

  test('should create a task via modal and display it in column @smoke @critical', async ({
    page,
  }) => {
    const { boardId, col1 } = await setupBoard(page, token)
    await page.goto(`/boards/${boardId}`)
    await page.waitForSelector('.column')
    await page.locator('.column__add').first().click()
    await expect(page.locator('[role="dialog"]')).toBeVisible()
    const title = `Task ${Date.now().toString(36)}`
    await page.locator('.modal__input').first().fill(title)
    await page.getByRole('button', { name: 'Create' }).click()
    await expect(page.getByText(title)).toBeVisible()
  })

  test('should open task detail page when clicking a task @smoke @critical', async ({
    page,
  }) => {
    const { boardId, col1 } = await setupBoard(page, token)
    const taskTitle = `Task ${Date.now().toString(36)}`
    await page.goto(`/boards/${boardId}`)
    await page.waitForSelector('.column')
    await page.locator('.column__add').first().click()
    await page.locator('.modal__input').first().fill(taskTitle)
    await page.getByRole('button', { name: 'Create' }).click()
    await page.waitForSelector('.task-card')
    await page.locator('.task-card').first().click()
    await page.waitForURL(/\/tasks\//)
    await expect(page.getByText(taskTitle)).toBeVisible()
  })

  test('should update task title and description on detail page @regression', async ({
    page,
  }) => {
    const { boardId, col1 } = await setupBoard(page, token)
    const taskTitle = `Task ${Date.now().toString(36)}`
    await page.goto(`/boards/${boardId}`)
    await page.waitForSelector('.column')
    await page.locator('.column__add').first().click()
    await page.locator('.modal__input').first().fill(taskTitle)
    await page.getByRole('button', { name: 'Create' }).click()
    await page.waitForSelector('.task-card')
    await page.locator('.task-card').first().click()
    await page.waitForURL(/\/tasks\//)
    await page.getByRole('button', { name: 'Edit' }).click()
    const updatedTitle = taskTitle + ' (edited)'
    await page.locator('.task-detail__input').first().fill(updatedTitle)
    await page.getByRole('button', { name: 'Save' }).click()
    await expect(page.getByText(updatedTitle)).toBeVisible()
  })

  test('should move task between columns via dropdown menu @regression', async ({
    page,
  }) => {
    const { boardId, col1, col2 } = await setupBoard(page, token)
    const taskTitle = `Task ${Date.now().toString(36)}`
    await page.goto(`/boards/${boardId}`)
    await page.waitForSelector('.column')
    await page.locator('.column__add').first().click()
    await page.locator('.modal__input').first().fill(taskTitle)
    await page.getByRole('button', { name: 'Create' }).click()
    await page.waitForSelector('.task-card')
    await page.locator('.task-card__action', { hasText: 'Move' }).click()
    const menuItems = page.locator('.task-card__menu-item')
    const secondColumn = menuItems.nth(1)
    const colName = await secondColumn.textContent()
    await secondColumn.click()
    await expect(page.getByText(taskTitle)).toBeVisible()
  })

  test('should archive a task @regression', async ({ page }) => {
    const { boardId, col1 } = await setupBoard(page, token)
    const taskTitle = `Task ${Date.now().toString(36)}`
    await page.goto(`/boards/${boardId}`)
    await page.waitForSelector('.column')
    await page.locator('.column__add').first().click()
    await page.locator('.modal__input').first().fill(taskTitle)
    await page.getByRole('button', { name: 'Create' }).click()
    await page.waitForSelector('.task-card')
    await page.locator('.task-card__action', { hasText: 'Archive' }).click()
    await expect(page.getByText(taskTitle)).not.toBeVisible()
  })

  test('should delete a task with confirmation @regression', async ({ page }) => {
    const { boardId, col1 } = await setupBoard(page, token)
    const taskTitle = `Task ${Date.now().toString(36)}`
    await page.goto(`/boards/${boardId}`)
    await page.waitForSelector('.column')
    await page.locator('.column__add').first().click()
    await page.locator('.modal__input').first().fill(taskTitle)
    await page.getByRole('button', { name: 'Create' }).click()
    await page.waitForSelector('.task-card')
    await page.locator('.task-card__action--danger', { hasText: 'Delete' }).click()
    await page.locator('.task-card__action--danger', { hasText: 'Yes' }).click()
    await expect(page.getByText(taskTitle)).not.toBeVisible()
  })
})
