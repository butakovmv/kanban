package com.kanban.postgres.task

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

/**
 * Таблица `comments` — хранит комментарии к задачам.
 * Каждый комментарий принадлежит задаче, имеет автора, текст и метки времени создания/обновления.
 */
@Table("comments")
internal data class CommentTable(
    @Id
    val id: String,
    val taskId: String,
    val authorId: String,
    val text: String,
    val createdAt: java.time.LocalDateTime,
    val updatedAt: java.time.LocalDateTime,
)
