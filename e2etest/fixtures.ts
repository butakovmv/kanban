import { test as base, expect, type TestInfo } from '@playwright/test'
import * as fs from 'fs'

export { expect }

interface ErrorEntry {
  testName: string
  testFile: string
  consoleErrors: string[]
  serverErrors: { url: string; status: number; statusText: string }[]
}

const errorsLog: ErrorEntry[] = []

function saveErrors() {
  const filtered = errorsLog.filter(
    (e) => e.consoleErrors.length > 0 || e.serverErrors.length > 0,
  )
  try {
    fs.writeFileSync('./errors-report.json', JSON.stringify(filtered, null, 2))
    if (filtered.length > 0) {
      console.log(`\nCollected ${filtered.length} test(s) with errors`)
    }
  } catch (err) {
    console.error('Failed to save errors:', err)
  }
}

process.on('exit', () => saveErrors())
process.on('SIGINT', () => {
  saveErrors()
  process.exit(1)
})

/**
 * Расширенный test с авто-мониторингом:
 * - собирает все 5xx ответы сервера
 * - собирает все console.error сообщения
 * - после каждого теста проверяет, что их нет
 */
export const test = base.extend<object>({
  autoMonitor: [
    async ({ page }, use, testInfo: TestInfo) => {
      const consoleErrors: string[] = []
      const serverErrors: { url: string; status: number; statusText: string }[] = []

      page.on('console', (msg) => {
        if (msg.type() === 'error') {
          consoleErrors.push(msg.text())
        }
      })

      page.on('response', async (response) => {
        const status = response.status()
        if (status >= 500) {
          let statusText = ''
          try {
            const body = await response.text()
            statusText = body.substring(0, 500)
          } catch {
            statusText = 'Unable to read response body'
          }
          serverErrors.push({ url: response.url(), status, statusText })
        }
      })

      await use()

      errorsLog.push({
        testName: testInfo.title,
        testFile: testInfo.file,
        consoleErrors: [...consoleErrors],
        serverErrors: [...serverErrors],
      })

      const messages: string[] = []
      if (consoleErrors.length > 0) {
        messages.push(`Console errors: ${consoleErrors.join('; ')}`)
      }
      if (serverErrors.length > 0) {
        messages.push(
          `Server 5xx responses: ${serverErrors.map((e) => `${e.status} ${e.url}`).join('; ')}`,
        )
      }
      expect(messages).toEqual([])
    },
    { auto: true },
  ],
})
