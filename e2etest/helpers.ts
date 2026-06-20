import { type Page, expect } from '@playwright/test'

const TEST_PASSWORD = 'test-password-123'

export function uniqueSuffix(): string {
  return Date.now().toString(36) + Math.random().toString(36).slice(2, 6)
}

export function testEmail(label: string): string {
  return `e2e-${label}-${uniqueSuffix()}@kanban.test`
}

export async function registerUser(page: Page, email: string): Promise<void> {
  await page.goto('/register')
  await page.getByRole('textbox', { name: /name/i }).fill('Test User')
  await page.getByRole('textbox', { name: /email/i }).fill(email)
  await page.getByRole('textbox', { name: /password/i }).fill(TEST_PASSWORD)
  await page.getByRole('button', { name: 'Register' }).click()
  await expect(page).toHaveURL(/\/login/)
}

export async function login(page: Page, email: string): Promise<string> {
  const tokenPromise = page.waitForResponse(
    (resp) =>
      resp.url().includes('/api/v1/auth/login') && resp.status() === 200,
  )
  await page.goto('/login')
  await page.getByRole('textbox', { name: /email/i }).fill(email)
  await page.getByRole('textbox', { name: /password/i }).fill(TEST_PASSWORD)
  await page.getByRole('button', { name: 'Login' }).click()
  await page.waitForURL(/\/board|\/projects|\/login/)
  const response = await tokenPromise
  const data = await response.json()
  return data.accessToken
}

export async function loginAsNewUser(
  page: Page,
  label: string,
): Promise<{ email: string; token: string }> {
  const email = testEmail(label)
  await registerUser(page, email)
  const token = await login(page, email)
  return { email, token }
}

async function api(
  page: Page,
  method: string,
  path: string,
  token: string,
  data?: Record<string, unknown>,
) {
  const res = await page.request.fetch(`/api/v1${path}`, {
    method,
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    data,
  })
  expect(res.ok()).toBeTruthy()
  return res.json()
}

export function apiGet(page: Page, path: string, token: string) {
  return api(page, 'GET', path, token)
}

export function apiPost(
  page: Page,
  path: string,
  token: string,
  data: Record<string, unknown>,
) {
  return api(page, 'POST', path, token, data)
}

export function apiPut(
  page: Page,
  path: string,
  token: string,
  data: Record<string, unknown>,
) {
  return api(page, 'PUT', path, token, data)
}

export function apiDel(page: Page, path: string, token: string) {
  return api(page, 'DELETE', path, token)
}

export async function createProjectViaUi(
  page: Page,
  name: string,
): Promise<string> {
  await page.goto('/projects')
  await page.getByRole('button', { name: /new project/i }).click()
  await page.getByRole('textbox', { name: /name/i }).fill(name)
  await page.getByRole('button', { name: 'Create' }).click()
  await page.waitForSelector('.project-list__item')
  const link = page.locator('.project-list__link').first()
  await expect(link).toBeVisible()
  const href = (await link.getAttribute('href')) ?? ''
  const match = href.match(/\/projects\/([^/]+)/)
  return match ? match[1] : ''
}

export async function createBoardViaApi(
  page: Page,
  projectId: string,
  boardName: string,
  token: string,
): Promise<{ boardId: string }> {
  const body = await apiPost(page, '/boards', token, {
    project_id: projectId,
    name: boardName,
  })
  return { boardId: body.id }
}

export async function createColumnViaApi(
  page: Page,
  boardId: string,
  columnName: string,
  token: string,
): Promise<string> {
  const body = await apiPost(page, `/boards/${boardId}/columns`, token, {
    name: columnName,
  })
  return body.id
}

export async function createTaskViaApi(
  page: Page,
  boardId: string,
  columnId: string,
  title: string,
  token: string,
): Promise<string> {
  const body = await apiPost(page, '/tasks', token, {
    board_id: boardId,
    column_id: columnId,
    title,
  })
  return body.id
}

export async function createCommentViaApi(
  page: Page,
  taskId: string,
  text: string,
  token: string,
): Promise<string> {
  const body = await apiPost(page, `/tasks/${taskId}/comments`, token, {
    text,
  })
  return body.id
}

export async function createDocumentViaApi(
  page: Page,
  projectId: string,
  title: string,
  token: string,
): Promise<string> {
  const body = await apiPost(page, '/documents', token, {
    project_id: projectId,
    title,
    file_name: 'test.txt',
    content_type: 'text/plain',
    content_base64: Buffer.from('hello world').toString('base64'),
    uploaded_by: 'test-user',
  })
  return body.id
}

export async function createGroupViaApi(
  page: Page,
  name: string,
  token: string,
): Promise<string> {
  const body = await apiPost(page, '/groups', token, { name })
  return body.id
}
