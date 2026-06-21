import { test, expect } from '../fixtures'

/** Пример E2E-теста. Группа smoke-тестов для проверки базовой загрузки страниц. */
test.describe('App', () => {
  /**
   * Проверяет, что главная страница загружается и содержит "Kanban" в заголовке.
   * @tag smoke
   */
  test('homepage loads', { tag: '@smoke' }, async ({ page }) => {
    await page.goto('/')
    await expect(page).toHaveTitle(/Kanban/)
  })
})
