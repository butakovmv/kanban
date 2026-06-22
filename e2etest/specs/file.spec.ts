import { test, expect } from '../fixtures'
import {
  loginAsNewUser,
  createProjectViaUi,
  createBoardViaApi,
  createTaskViaApi,
} from '../helpers'

test.describe('File attachment operations', () => {
  let token: string

  test.beforeEach(async ({ page }) => {
    const creds = await loginAsNewUser(page, 'file')
    token = creds.token
  })

  async function setupTask(page: any, token: string): Promise<string> {
    const projectId = await createProjectViaUi(
      page,
      `FileProj ${Date.now().toString(36)}`,
    )
    const { boardId, columnIds } = await createBoardViaApi(
      page,
      projectId,
      'File Board',
      token,
    )
    const taskId = await createTaskViaApi(
      page,
      boardId,
      columnIds[0],
      `FileTask ${Date.now().toString(36)}`,
      token,
    )
    return taskId
  }

  test('should show file upload section on task detail @smoke @critical', async ({
    page,
  }) => {
    const taskId = await setupTask(page, token)
    await page.goto(`/tasks/${taskId}`)
    await page.waitForSelector('.task-detail__card')
    await expect(page.locator('.files')).toBeVisible()
    await expect(page.getByRole('heading', { name: /files/i })).toBeVisible()
  })

  test('should display file chooser on drag area click @regression', async ({
    page,
  }) => {
    const taskId = await setupTask(page, token)
    await page.goto(`/tasks/${taskId}`)
    await page.waitForSelector('.task-detail__card')
    const fileChooserPromise = page.waitForEvent('filechooser')
    await page.locator('.files__browse').click()
    const fileChooser = await fileChooserPromise
    expect(fileChooser).toBeTruthy()
  })

  test('should show file in list after upload @regression', async ({ page }) => {
    const taskId = await setupTask(page, token)
    await page.goto(`/tasks/${taskId}`)
    await page.waitForSelector('.task-detail__card')
    const fileChooserPromise = page.waitForEvent('filechooser')
    await page.locator('.files__browse').click()
    const fileChooser = await fileChooserPromise
    await fileChooser.setFiles({
      name: 'test.txt',
      mimeType: 'text/plain',
      buffer: Buffer.from('hello'),
    })
    const fileName = page.locator('.files__name')
    await expect(fileName).toBeVisible()
  })

  test('should have delete file button on each file @regression', async ({
    page,
  }) => {
    const taskId = await setupTask(page, token)
    await page.goto(`/tasks/${taskId}`)
    await page.waitForSelector('.task-detail__card')
    const fileChooserPromise = page.waitForEvent('filechooser')
    await page.locator('.files__browse').click()
    const fileChooser = await fileChooserPromise
    await fileChooser.setFiles({
      name: 'test.txt',
      mimeType: 'text/plain',
      buffer: Buffer.from('hello'),
    })
    await expect(page.locator('.files__remove')).toBeVisible()
  })
})
