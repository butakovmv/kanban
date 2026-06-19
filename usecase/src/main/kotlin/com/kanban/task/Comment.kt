package com.kanban.task

import com.kanban.common.CommentId
import com.kanban.common.TaskId
import java.time.Instant

/**
 * Сущность комментария к задаче.
 * Содержит текст комментария, автора и временные метки создания/обновления.
 *
 * @property id уникальный идентификатор комментария
 * @property taskId идентификатор задачи, к которой относится комментарий
 * @property authorId идентификатор пользователя-автора комментария
 * @property text текст комментария
 * @property createdAt дата и время создания
 * @property updatedAt дата и время последнего изменения
 */
data class Comment(
    val id: CommentId,
    val taskId: TaskId,
    val authorId: String,
    val text: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
