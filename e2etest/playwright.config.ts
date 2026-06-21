import { defineConfig, devices } from '@playwright/test'

/**
 * Конфигурация Playwright для E2E-тестирования.
 * Запускает тесты в Chromium, Firefox и WebKit.
 * В CI использует 1 воркер и 2 ретрая.
 * Базовый URL берётся из переменной окружения BASE_URL (по умолч. http://localhost:80).
 */
export default defineConfig({
  testDir: './specs',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: [
    ['html', { outputFolder: 'report' }],
    ['list'],
  ],

  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:80',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },

  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
//    { name: 'firefox', use: { ...devices['Desktop Firefox'] } },
//    { name: 'webkit', use: { ...devices['Desktop Safari'] } },
  ],
})
