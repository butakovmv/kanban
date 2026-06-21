import { test as base, expect } from '@playwright/test'

export { expect }

/**
 * Расширенный test с авто-мониторингом:
 * - собирает все 5xx ответы сервера
 * - собирает все console.error сообщения
 * - после каждого теста проверяет, что их нет
 */
export const test = base.extend<object>({
  autoMonitor: [
    async ({ page }, use) => {
      const consoleErrors: string[] = []
      const serverErrors: { url: string; status: number }[] = []

      page.on('console', (msg) => {
        if (msg.type() === 'error') {
          consoleErrors.push(msg.text())
        }
      })

      page.on('response', (response) => {
        const status = response.status()
        if (status >= 500) {
          serverErrors.push({ url: response.url(), status })
        }
      })

      await use()

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
