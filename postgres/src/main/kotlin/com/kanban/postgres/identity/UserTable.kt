package com.kanban.postgres.identity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

/**
 * Таблица `users` — хранит учётные записи пользователей.
 * Содержит email, хеш пароля, отображаемое имя, настройки TOTP-аутентификации,
 * а также метки времени создания и обновления записи.
 */
@Table("users")
internal data class UserTable(
    @Id
    val id: String,
    val email: String,
    val passwordHash: String,
    val displayName: String,
    val totpSecret: String?,
    val totpEnabled: Boolean,
    val createdAt: java.time.LocalDateTime,
    val updatedAt: java.time.LocalDateTime,
)
