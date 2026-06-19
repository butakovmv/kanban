package com.kanban.postgres.task

import com.kanban.common.BoardId
import com.kanban.common.ColumnId
import com.kanban.common.TaskId
import com.kanban.task.Task
import java.time.ZoneId

/**
 * Маппинг табличной сущности [TaskTable] в доменную сущность [Task].
 * Преобразует сырые типы (String, LocalDateTime) в типы предметной области
 * (TaskId, BoardId, ColumnId, Instant).
 */
internal fun TaskTable.toDomain(): Task =
    Task(
        id = TaskId(id),
        boardId = BoardId(boardId),
        columnId = ColumnId(columnId),
        title = title,
        description = description,
        assigneeId = assigneeId,
        position = position,
        dueDate = dueDate?.atZone(ZoneId.systemDefault())?.toInstant(),
        archived = archived,
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toInstant(),
        updatedAt = updatedAt.atZone(ZoneId.systemDefault()).toInstant(),
    )

/**
 * Маппинг доменной сущности [Task] в табличную сущность [TaskTable].
 * Обратное преобразование типов предметной области в сырые типы для сохранения в БД.
 */
internal fun Task.toTable(): TaskTable =
    TaskTable(
        id = id.value,
        boardId = boardId.value,
        columnId = columnId.value,
        title = title,
        description = description,
        assigneeId = assigneeId,
        position = position,
        dueDate = dueDate?.atZone(ZoneId.systemDefault())?.toLocalDateTime(),
        archived = archived,
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
        updatedAt = updatedAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
    )
