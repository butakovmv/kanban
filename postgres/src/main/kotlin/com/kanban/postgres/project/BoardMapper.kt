package com.kanban.postgres.project

import com.kanban.common.BoardId
import com.kanban.common.ProjectId
import com.kanban.project.Board
import java.time.ZoneId

/**
 * Маппинг табличной сущности [BoardTable] в доменную сущность [Board].
 * Преобразует сырые типы (String, LocalDateTime) в типы предметной области (BoardId, ProjectId, Instant).
 * Поле `archived` намеренно не пробрасывается в доменную сущность.
 */
internal fun BoardTable.toDomain(): Board =
    Board(
        id = BoardId(id),
        projectId = ProjectId(projectId),
        name = name,
        position = position,
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toInstant(),
    )

/**
 * Маппинг доменной сущности [Board] в табличную сущность [BoardTable].
 * При создании новой записи флаг `archived` по умолчанию равен false.
 * Обратное преобразование типов предметной области в сырые типы для сохранения в БД.
 */
internal fun Board.toTable(): BoardTable =
    BoardTable(
        id = id.value,
        projectId = projectId.value,
        name = name,
        position = position,
        archived = false,
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
    )
