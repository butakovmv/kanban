package com.kanban.common

/**
 * Value-объект, содержащий пару токенов: access и refresh.
 * Возвращается при успешной аутентификации или регистрации.
 */
data class AuthTokens(
    val accessToken: AccessToken,
    val refreshToken: RefreshToken,
)
