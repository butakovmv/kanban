package com.kanban.postgres.project

import com.kanban.common.BoardId
import com.kanban.common.ColumnId
import com.kanban.project.Column
import java.time.ZoneId

/**
 * Маппинг табличной сущности [ColumnTable] в доменную сущность [Column].
 * Преобразует сырые типы (String, Int?, LocalDateTime) в типы предметной области
 * (ColumnId, BoardId, Instant).
 */
internal fun ColumnTable.toDomain(): Column =
    Column(
        id = ColumnId(id),
        boardId = BoardId(boardId),
        name = name,
        position = position,
        wipLimit = wipLimit,
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toInstant(),
    )

/**
 * Маппинг доменной сущности [Column] в табличную сущность [ColumnTable].
 * Обратное преобразование типов предметной области в сырые типы для сохранения в БД.
 */
internal fun Column.toTable(): ColumnTable =
    ColumnTable(
        id = id.value,
        boardId = boardId.value,
        name = name,
        position = position,
        wipLimit = wipLimit,
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
    )
