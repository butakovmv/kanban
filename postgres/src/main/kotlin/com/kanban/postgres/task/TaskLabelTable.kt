package com.kanban.postgres.task

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

/**
 * Таблица `task_labels` — связь задач с метками.
 * Каждая запись связывает задачу с одной меткой.
 */
@Table("task_labels")
internal data class TaskLabelTable(
    @Id
    val taskId: String,
    @Column("label")
    val label: String,
)
