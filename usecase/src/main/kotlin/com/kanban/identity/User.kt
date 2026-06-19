package com.kanban.identity

import com.kanban.common.Email
import com.kanban.common.PasswordHash
import com.kanban.common.UserId
import java.time.Instant

/**
 * Сущность пользователя системы.
 * Содержит учётные данные (email, хеш пароля), профильную информацию и настройки двухфакторной аутентификации.
 */
data class User(
    val id: UserId,
    val email: Email,
    val passwordHash: PasswordHash,
    val displayName: String,
    val totpSecret: String?,
    val totpEnabled: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)
