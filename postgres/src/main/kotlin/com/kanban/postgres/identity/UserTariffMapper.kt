package com.kanban.postgres.identity

import com.kanban.identity.UserTariff
import java.time.ZoneId

/**
 * Маппинг табличной сущности [UserTariffTable] в доменную сущность [UserTariff].
 * Преобразует LocalDateTime в Instant для всех временных полей.
 */
internal fun UserTariffTable.toDomain(): UserTariff =
    UserTariff(
        id = id,
        userId = userId,
        tariffId = tariffId,
        startsAt = startsAt.atZone(ZoneId.systemDefault()).toInstant(),
        expiresAt = expiresAt?.atZone(ZoneId.systemDefault())?.toInstant(),
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toInstant(),
    )

/**
 * Маппинг доменной сущности [UserTariff] в табличную сущность [UserTariffTable].
 * Обратное преобразование Instant в LocalDateTime для сохранения в БД.
 */
internal fun UserTariff.toTable(): UserTariffTable =
    UserTariffTable(
        id = id,
        userId = userId,
        tariffId = tariffId,
        startsAt = startsAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
        expiresAt = expiresAt?.atZone(ZoneId.systemDefault())?.toLocalDateTime(),
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
    )
