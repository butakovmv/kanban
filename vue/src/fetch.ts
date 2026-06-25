/** Базовый URL для всех запросов к API. */
const BASE_URL = '/api/v1'

/** Текущий access-токен для авторизации запросов. */
let accessToken: string | null = null

/** Функция обновления токена, устанавливается из auth store. */
let refreshFn: (() => Promise<boolean>) | null = null

/** Таймаут запроса по умолчанию (30 с). */
const REQUEST_TIMEOUT_MS = 30_000

/** Флаг предотвращения одновременного обновления токена несколькими запросами. */
let isRefreshing = false
let refreshPromise: Promise<boolean> | null = null

/**
 * Устанавливает access-токен для последующих запросов к API.
 * @param token - токен или null для сброса
 */
export function setAccessToken(token: string | null) {
  accessToken = token
}

/**
 * Возвращает текущий access-токен.
 * @returns токен или null, если токен не установлен
 */
export function getAccessToken(): string | null {
  return accessToken
}

/**
 * Устанавливает функцию для обновления токена при 401.
 * @param fn - асинхронная функция, возвращающая true при успешном обновлении
 */
export function setRefreshFn(fn: (() => Promise<boolean>) | null) {
  refreshFn = fn
}

async function doFetch(url: string, options: RequestInit): Promise<Response> {
  const controller = new AbortController()
  const id = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS)
  try {
    return await fetch(url, { ...options, signal: controller.signal })
  } finally {
    clearTimeout(id)
  }
}

/**
 * Парсит ответ как JSON с безопасной обработкой ошибки.
 */
async function parseJson<T>(response: Response): Promise<T> {
  try {
    return await (response.json() as Promise<T>)
  } catch {
    const text = await response.text().catch(() => '')
    throw new Error(`Invalid JSON response: ${text.slice(0, 200)}`)
  }
}

/**
 * Базовый HTTP-запрос с автоматической подстановкой заголовков и токена.
 * Поддерживает автоматическое обновление токена при 401.
 * @param url - относительный путь (добавляется к BASE_URL)
 * @param options - стандартные опции fetch
 * @returns ответ, преобразованный в JSON типом T
 * @throws Error при статусе ответа не из диапазона 2xx
 */
async function request<T>(url: string, options: RequestInit = {}): Promise<T> {
  async function buildHeaders(): Promise<Record<string, string>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      'X-Request-Id': crypto.randomUUID(),
      ...(options.headers as Record<string, string>),
    }
    if (accessToken) {
      headers['Authorization'] = `Bearer ${accessToken}`
    }
    return headers
  }

  let response = await doFetch(`${BASE_URL}${url}`, {
    ...options,
    headers: await buildHeaders(),
  })

  if (response.status === 401 && refreshFn) {
    if (!isRefreshing) {
      isRefreshing = true
      refreshPromise = refreshFn().finally(() => {
        isRefreshing = false
        refreshPromise = null
      })
    }
    const refreshed = await refreshPromise
    if (refreshed) {
      response = await doFetch(`${BASE_URL}${url}`, {
        ...options,
        headers: await buildHeaders(),
      })
    }
  }

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: response.statusText }))
    throw new Error(error.message || response.statusText)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return parseJson<T>(response)
}

/**
 * GET-запрос к API.
 * @param url - относительный путь
 * @returns ответ типом T
 */
export function get<T>(url: string): Promise<T> {
  return request<T>(url)
}

/**
 * POST-запрос к API с JSON-телом.
 * @param url - относительный путь
 * @param body - тело запроса (сериализуется в JSON)
 * @returns ответ типом T
 */
export function post<T>(url: string, body: unknown): Promise<T> {
  return request<T>(url, {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

/**
 * PUT-запрос к API с JSON-телом.
 * @param url - относительный путь
 * @param body - тело запроса (сериализуется в JSON)
 * @returns ответ типом T
 */
export function put<T>(url: string, body: unknown): Promise<T> {
  return request<T>(url, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
}

/**
 * DELETE-запрос к API.
 * @param url - относительный путь
 * @returns ответ типом T
 */
export function del<T>(url: string): Promise<T> {
  return request<T>(url, {
    method: 'DELETE',
  })
}

/**
 * PATCH-запрос к API с JSON-телом.
 * @param url - относительный путь
 * @param body - тело запроса (сериализуется в JSON)
 * @returns ответ типом T
 */
export function patch<T>(url: string, body: unknown): Promise<T> {
  return request<T>(url, {
    method: 'PATCH',
    body: JSON.stringify(body),
  })
}
