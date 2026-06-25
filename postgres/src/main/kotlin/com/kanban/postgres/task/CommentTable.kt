package com.kanban.postgres.task

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

/**
 * Таблица `comments` — хранит комментарии к задачам.
 * Каждый комментарий принадлежит задаче, имеет автора, текст и метки времени создания/обновления.
 */
@Table("comments")
internal data class CommentTable(
    @Id
    val id: String,
    @Column("task_id")
    val taskId: String,
    @Column("author_id")
    val authorId: String,
    @Column("text")
    val text: String,
    @Column("created_at")
    val createdAt: java.time.LocalDateTime,
    @Column("updated_at")
    val updatedAt: java.time.LocalDateTime,
)
