package com.kanban.identity

import com.kanban.common.TariffId
import java.time.Instant

data class Tariff(
    val id: TariffId,
    val name: String,
    val limits: TariffLimits,
    val createdAt: Instant,
)
