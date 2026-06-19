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
  return post<AuthResponse>('/auth/register', {
    email: request.email,
    password: request.password,
    display_name: request.displayName,
  })
}

/**
 * Аутентифицирует пользователя по email и паролю.
 * @param request email, password
 * @returns токены и информация о пользователе
 */
export function login(request: LoginRequest): Promise<AuthResponse> {
  return post<AuthResponse>('/auth/login', {
    email: request.email,
    password: request.password,
  })
}

/**
 * Обновляет пару токенов по refresh-токену.
 * @param request refreshToken
 * @returns новая пара токенов
 */
export function refresh(request: RefreshRequest): Promise<AuthTokens> {
  return post<AuthTokens>('/auth/refresh', {
    refresh_token: request.refreshToken,
  })
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
