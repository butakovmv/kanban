package com.kanban.identity

import java.time.Instant

/**
 * Сущность, связывающая пользователя с его тарифом.
 * Содержит период действия тарифа (startsAt — expiresAt) и ссылки на пользователя и тариф.
 */
data class UserTariff(
    val id: String,
    val userId: String,
    val tariffId: String,
    val startsAt: Instant,
    val expiresAt: Instant?,
    val createdAt: Instant,
)
