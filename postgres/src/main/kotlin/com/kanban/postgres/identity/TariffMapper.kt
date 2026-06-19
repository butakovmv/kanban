package com.kanban.postgres.identity

import com.kanban.common.TariffId
import com.kanban.identity.Tariff
import com.kanban.identity.TariffLimits
import java.time.ZoneId

/**
 * Маппинг табличной сущности [TariffTable] в доменную сущность [Tariff].
 * Преобразует поля лимитов из отдельных примитивов в структуру [TariffLimits], а LocalDateTime в Instant.
 */
internal fun TariffTable.toDomain(): Tariff =
    Tariff(
        id = TariffId(id),
        name = name,
        limits =
            TariffLimits(
                maxProjects = maxProjects,
                maxBoardsPerProject = maxBoardsPerProject,
                maxTasksPerBoard = maxTasksPerBoard,
                maxFileSizeMb = maxFileSizeMb,
                maxStorageMb = maxStorageMb,
            ),
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toInstant(),
    )

/**
 * Маппинг доменной сущности [Tariff] в табличную сущность [TariffTable].
 * Разворачивает структуру [TariffLimits] в отдельные поля и преобразует Instant в LocalDateTime.
 */
internal fun Tariff.toTable(): TariffTable =
    TariffTable(
        id = id.value,
        name = name,
        maxProjects = limits.maxProjects,
        maxBoardsPerProject = limits.maxBoardsPerProject,
        maxTasksPerBoard = limits.maxTasksPerBoard,
        maxFileSizeMb = limits.maxFileSizeMb,
        maxStorageMb = limits.maxStorageMb,
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
    )
