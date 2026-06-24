package com.kanban.postgres.task

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

/**
 * Таблица `tasks` — хранит задачи (карточки) проекта.
 * Каждая задача принадлежит проекту и находится в колонке; имеет заголовок, описание,
 * опционального исполнителя, позицию для упорядочивания, опциональный срок выполнения,
 * флаг архивации и метки времени создания/обновления.
 */
@Table("tasks")
internal data class TaskTable(
    @Id
    val id: String,
    val projectId: String,
    val columnId: String,
    val title: String,
    val description: String?,
    val assigneeId: String?,
    val position: Int,
    val dueDate: java.time.LocalDateTime?,
    val priority: String?,
    val archived: Boolean,
    val createdAt: java.time.LocalDateTime,
    val updatedAt: java.time.LocalDateTime,
)
