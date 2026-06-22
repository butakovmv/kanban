import { get } from '../../fetch'
import { post } from '../../fetch'

/**
 * Тип пользователя, возвращаемый сервером после аутентификации.
 */
export interface AuthUser {
  id: string
  email: string
  displayName: string
}

/**
 * Пара токенов аутентификации.
 */
export interface AuthTokens {
  accessToken: string
  refreshToken: string
}

/**
 * Ответ сервера на успешную регистрацию или вход.
 */
export interface AuthResponse extends AuthTokens {
  user: AuthUser
}

/* ───── raw snake‑case types, matching backend JSON ───── */

interface RawAuthTokens {
  access_token: string
  refresh_token: string
}

interface RawAuthUser {
  id: string
  email: string
  display_name: string
}

interface RawAuthResponse extends RawAuthTokens {
  user: RawAuthUser
}

function toAuthResponse(raw: RawAuthResponse): AuthResponse {
  return {
    accessToken: raw.access_token,
    refreshToken: raw.refresh_token,
    user: {
      id: raw.user.id,
      email: raw.user.email,
      displayName: raw.user.display_name,
    },
  }
}

function toAuthTokens(raw: RawAuthTokens): AuthTokens {
  return {
    accessToken: raw.access_token,
    refreshToken: raw.refresh_token,
  }
}

/**
 * Информация о тарифе пользователя.
 */
export interface TariffInfo {
  name: string
  maxProjects: number
  maxBoardsPerProject: number
  maxTasksPerBoard: number
  maxFileSizeMb: number
  maxStorageMb: number
}

/**
 * Параметры запроса регистрации.
 */
export interface RegisterRequest {
  email: string
  password: string
  displayName: string
}

/**
 * Параметры запроса входа.
 */
export interface LoginRequest {
  email: string
  password: string
}

/**
 * Параметры запроса обновления токена.
 */
export interface RefreshRequest {
  refreshToken: string
}

/**
 * Параметры запроса выхода.
 */
export interface LogoutRequest {
  refreshToken: string
}

/**
 * Регистрирует нового пользователя.
 * @param request email, password, displayName
 * @returns токены и информация о пользователе
 */
export function register(request: RegisterRequest): Promise<AuthResponse> {
  return post<RawAuthResponse>('/auth/register', {
    email: request.email,
    password: request.password,
    display_name: request.displayName,
  }).then(toAuthResponse)
}

/**
 * Аутентифицирует пользователя по email и паролю.
 * @param request email, password
 * @returns токены и информация о пользователе
 */
export function login(request: LoginRequest): Promise<AuthResponse> {
  return post<RawAuthResponse>('/auth/login', {
    email: request.email,
    password: request.password,
  }).then(toAuthResponse)
}

/**
 * Обновляет пару токенов по refresh-токену.
 * @param request refreshToken
 * @returns новая пара токенов
 */
export function refresh(request: RefreshRequest): Promise<AuthTokens> {
  return post<RawAuthTokens>('/auth/refresh', {
    refresh_token: request.refreshToken,
  }).then(toAuthTokens)
}

/**
 * Аннулирует refresh-токен пользователя.
 * @param request refreshToken
 */
export function logout(request: LogoutRequest): Promise<void> {
  return post<void>('/auth/logout', {
    refresh_token: request.refreshToken,
  })
}

export function getTariff(userId: string): Promise<TariffInfo> {
  return get<TariffInfo>(`/profile/tariff?user_id=${encodeURIComponent(userId)}`)
}
