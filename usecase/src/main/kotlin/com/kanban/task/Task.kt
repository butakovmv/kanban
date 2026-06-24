package com.kanban.task

import com.kanban.common.ColumnId
import com.kanban.common.ProjectId
import com.kanban.common.TaskId
import java.time.Instant

/**
 * Сущность задачи — элемента проекта, размещаемого в колонке и упорядоченного по позиции.
 * Содержит основные атрибуты задачи: заголовок, описание, исполнитель, срок и признак архивации.
 *
 * @property id уникальный идентификатор задачи
 * @property projectId идентификатор проекта, к которому относится задача
 * @property columnId идентификатор колонки, в которой находится задача
 * @property title заголовок задачи
 * @property description описание задачи (опционально)
 * @property assigneeId идентификатор пользователя-исполнителя (опционально)
 * @property position позиция задачи в колонке (чем меньше, тем выше)
 * @property dueDate срок выполнения задачи (опционально)
 * @property priority приоритет: low, medium, high, critical (опционально)
 * @property archived признак архивации (архивные задачи скрыты из активных списков)
 * @property createdAt дата и время создания
 * @property updatedAt дата и время последнего изменения
 */
data class Task(
    val id: TaskId,
    val projectId: ProjectId,
    val columnId: ColumnId,
    val title: String,
    val description: String?,
    val assigneeId: String?,
    val position: Int,
    val dueDate: Instant?,
    val priority: String?,
    val archived: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)
