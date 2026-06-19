package com.kanban.identity

import java.time.Instant

data class UserTariff(
    val id: String,
    val userId: String,
    val tariffId: String,
    val startsAt: Instant,
    val expiresAt: Instant?,
    val createdAt: Instant,
)
