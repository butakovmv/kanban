package com.kanban.postgres.identity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

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
