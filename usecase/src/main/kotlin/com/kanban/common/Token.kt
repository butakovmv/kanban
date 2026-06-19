package com.kanban.common

/**
 * Value-объект, представляющий access-токен для аутентификации.
 */
data class AccessToken(
    val value: String,
)

/**
 * Value-объект, представляющий refresh-токен для обновления access-токена.
 */
data class RefreshToken(
    val value: String,
)
