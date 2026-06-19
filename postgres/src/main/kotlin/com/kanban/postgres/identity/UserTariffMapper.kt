package com.kanban.postgres.identity

import com.kanban.identity.UserTariff
import java.time.ZoneId

internal fun UserTariffTable.toDomain(): UserTariff =
    UserTariff(
        id = id,
        userId = userId,
        tariffId = tariffId,
        startsAt = startsAt.atZone(ZoneId.systemDefault()).toInstant(),
        expiresAt = expiresAt?.atZone(ZoneId.systemDefault())?.toInstant(),
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toInstant(),
    )

internal fun UserTariff.toTable(): UserTariffTable =
    UserTariffTable(
        id = id,
        userId = userId,
        tariffId = tariffId,
        startsAt = startsAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
        expiresAt = expiresAt?.atZone(ZoneId.systemDefault())?.toLocalDateTime(),
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
    )
