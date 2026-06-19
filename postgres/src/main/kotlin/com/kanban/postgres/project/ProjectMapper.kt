package com.kanban.postgres.project

import com.kanban.common.ProjectId
import com.kanban.common.UserId
import com.kanban.project.Project
import java.time.ZoneId

/**
 * Маппинг табличной сущности [ProjectTable] в доменную сущность [Project].
 * Преобразует сырые типы (String, LocalDateTime) в типы предметной области (ProjectId, UserId, Instant).
 */
internal fun ProjectTable.toDomain(): Project =
    Project(
        id = ProjectId(id),
        ownerId = UserId(ownerId),
        name = name,
        description = description,
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toInstant(),
        updatedAt = updatedAt.atZone(ZoneId.systemDefault()).toInstant(),
    )

/**
 * Маппинг доменной сущности [Project] в табличную сущность [ProjectTable].
 * Обратное преобразование типов предметной области в сырые типы для сохранения в БД.
 */
internal fun Project.toTable(): ProjectTable =
    ProjectTable(
        id = id.value,
        ownerId = ownerId.value,
        name = name,
        description = description,
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
        updatedAt = updatedAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
    )
