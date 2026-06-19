package com.kanban.postgres.task

import com.kanban.common.CommentId
import com.kanban.common.TaskId
import com.kanban.task.Comment
import java.time.ZoneId

/**
 * Маппинг табличной сущности [CommentTable] в доменную сущность [Comment].
 * Преобразует сырые типы (String, LocalDateTime) в типы предметной области
 * (CommentId, TaskId, Instant).
 */
internal fun CommentTable.toDomain(): Comment =
    Comment(
        id = CommentId(id),
        taskId = TaskId(taskId),
        authorId = authorId,
        text = text,
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toInstant(),
        updatedAt = updatedAt.atZone(ZoneId.systemDefault()).toInstant(),
    )

/**
 * Маппинг доменной сущности [Comment] в табличную сущность [CommentTable].
 * Обратное преобразование типов предметной области в сырые типы для сохранения в БД.
 */
internal fun Comment.toTable(): CommentTable =
    CommentTable(
        id = id.value,
        taskId = taskId.value,
        authorId = authorId,
        text = text,
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
        updatedAt = updatedAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
    )
