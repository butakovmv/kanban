package com.kanban.project

import com.kanban.common.ColumnId
import com.kanban.common.ProjectId
import java.time.Instant

/**
 * Сущность колонки проекта — вертикальной полосы для задач с определённым статусом.
 * Содержит позицию для упорядочивания колонок и опциональный WIP-лимит.
 *
 * @property id уникальный идентификатор колонки
 * @property projectId идентификатор проекта, к которому относится колонка
 * @property name название колонки
 * @property position позиция колонки в проекте (чем меньше, тем левее)
 * @property wipLimit лимит количества задач в колонке (work-in-progress), null — без ограничения
 * @property createdAt дата и время создания
 */
data class Column(
    val id: ColumnId,
    val projectId: ProjectId,
    val name: String,
    val position: Int,
    val wipLimit: Int?,
    val createdAt: Instant,
)
