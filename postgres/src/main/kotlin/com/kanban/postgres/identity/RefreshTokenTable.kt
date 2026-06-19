package com.kanban.postgres.identity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("refresh_tokens")
internal data class RefreshTokenTable(
    @Id
    val id: String,
    val userId: String,
    val tokenHash: String,
    val expiresAt: java.time.LocalDateTime,
    val createdAt: java.time.LocalDateTime,
)
