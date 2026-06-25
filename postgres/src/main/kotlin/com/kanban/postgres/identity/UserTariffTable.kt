package com.kanban.postgres.identity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

/**
 * Таблица `user_tariffs` — хранит назначения тарифных планов пользователям.
 * Содержит ссылки на пользователя и тариф, дату начала действия, дату истечения
 * (если применимо) и дату создания записи.
 */
@Table("user_tariffs")
internal data class UserTariffTable(
    @Id
    val id: String,
    @Column("user_id")
    val userId: String,
    @Column("tariff_id")
    val tariffId: String,
    @Column("starts_at")
    val startsAt: java.time.LocalDateTime,
    @Column("expires_at")
    val expiresAt: java.time.LocalDateTime?,
    @Column("created_at")
    val createdAt: java.time.LocalDateTime,
)
