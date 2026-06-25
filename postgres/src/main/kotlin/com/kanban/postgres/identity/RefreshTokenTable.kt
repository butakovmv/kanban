package com.kanban.postgres.identity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

/**
 * Таблица `refresh_tokens` — хранит refresh-токены для продления сессий пользователей.
 * Содержит идентификатор пользователя, хеш токена, дату истечения и дату создания.
 */
@Table("refresh_tokens")
internal data class RefreshTokenTable(
    @Id
    val id: String,
    @Column("user_id")
    val userId: String,
    @Column("token_hash")
    val tokenHash: String,
    @Column("expires_at")
    val expiresAt: java.time.LocalDateTime,
    @Column("created_at")
    val createdAt: java.time.LocalDateTime,
)
