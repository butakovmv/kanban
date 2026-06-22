/** Базовый URL для всех запросов к API. */
const BASE_URL = '/api/v1'

/** Текущий access-токен для авторизации запросов. */
let accessToken: string | null = null

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
 * Базовый HTTP-запрос с автоматической подстановкой заголовков и токена.
 * @param url - относительный путь (добавляется к BASE_URL)
 * @param options - стандартные опции fetch
 * @returns ответ, преобразованный в JSON типом T
 * @throws Error при статусе ответа не из диапазона 2xx
 */
async function request<T>(url: string, options: RequestInit = {}): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    'X-Request-Id': crypto.randomUUID(),
    ...(options.headers as Record<string, string>),
  }

  if (accessToken) {
    headers['Authorization'] = `Bearer ${accessToken}`
  }

  const response = await fetch(`${BASE_URL}${url}`, {
    ...options,
    headers,
  })

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: response.statusText }))
    throw new Error(error.message || response.statusText)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return response.json()
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
