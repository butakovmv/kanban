package com.kanban.postgres.identity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

/**
 * Таблица `refresh_tokens` — хранит refresh-токены для продления сессий пользователей.
 * Содержит идентификатор пользователя, хеш токена, дату истечения и дату создания.
 */
@Table("refresh_tokens")
internal data class RefreshTokenTable(
    @Id
    val id: String,
    val userId: String,
    val tokenHash: String,
    val expiresAt: java.time.LocalDateTime,
    val createdAt: java.time.LocalDateTime,
)
