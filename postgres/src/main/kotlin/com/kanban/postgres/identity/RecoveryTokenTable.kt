package com.kanban.postgres.identity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("recovery_tokens")
internal data class RecoveryTokenTable(
    @Id
    val id: String,
    val userId: String,
    val tokenHash: String,
    val expiresAt: java.time.LocalDateTime,
    val createdAt: java.time.LocalDateTime,
)
