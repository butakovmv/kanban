package com.kanban.postgres.task

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
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
    @Column("project_id")
    val projectId: String,
    @Column("column_id")
    val columnId: String,
    @Column("title")
    val title: String,
    @Column("description")
    val description: String?,
    @Column("assignee_id")
    val assigneeId: String?,
    @Column("position")
    val position: Int,
    @Column("due_date")
    val dueDate: java.time.LocalDateTime?,
    @Column("priority")
    val priority: String?,
    @Column("archived")
    val archived: Boolean,
    @Column("created_at")
    val createdAt: java.time.LocalDateTime,
    @Column("updated_at")
    val updatedAt: java.time.LocalDateTime,
)
