package com.kanban.project

import com.kanban.common.BoardId
import com.kanban.common.ColumnId
import java.time.Instant

/**
 * Сущность колонки доски — вертикальной полосы для задач с определённым статусом.
 * Содержит позицию для упорядочивания колонок и опциональный WIP-лимит.
 *
 * @property id уникальный идентификатор колонки
 * @property boardId идентификатор доски, к которой относится колонка
 * @property name название колонки
 * @property position позиция колонки на доске (чем меньше, тем левее)
 * @property wipLimit лимит количества задач в колонке (work-in-progress), null — без ограничения
 * @property createdAt дата и время создания
 */
data class Column(
    val id: ColumnId,
    val boardId: BoardId,
    val name: String,
    val position: Int,
    val wipLimit: Int?,
    val createdAt: Instant,
)
