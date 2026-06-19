package com.kanban.identity

import com.kanban.common.TariffId
import java.time.Instant

/**
 * Сущность тарифа — набора ограничений и возможностей для пользователя.
 * Определяет лимиты на количество проектов, досок, задач и объём хранилища.
 */
data class Tariff(
    val id: TariffId,
    val name: String,
    val limits: TariffLimits,
    val createdAt: Instant,
)
