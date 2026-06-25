package com.kanban.postgres.identity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
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
    @Column("email")
    val email: String,
    @Column("password_hash")
    val passwordHash: String,
    @Column("display_name")
    val displayName: String,
    @Column("totp_secret")
    val totpSecret: String?,
    @Column("totp_enabled")
    val totpEnabled: Boolean,
    @Column("created_at")
    val createdAt: java.time.LocalDateTime,
    @Column("updated_at")
    val updatedAt: java.time.LocalDateTime,
)
