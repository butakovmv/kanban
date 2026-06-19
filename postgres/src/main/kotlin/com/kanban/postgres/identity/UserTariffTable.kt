package com.kanban.postgres.identity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("user_tariffs")
internal data class UserTariffTable(
    @Id
    val id: String,
    val userId: String,
    val tariffId: String,
    val startsAt: java.time.LocalDateTime,
    val expiresAt: java.time.LocalDateTime?,
    val createdAt: java.time.LocalDateTime,
)
