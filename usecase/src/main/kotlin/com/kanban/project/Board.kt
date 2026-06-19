package com.kanban.project

import com.kanban.common.BoardId
import com.kanban.common.ProjectId
import java.time.Instant

/**
 * Сущность доски — набора колонок с задачами внутри проекта.
 * Содержит позицию для упорядочивания досок внутри проекта.
 *
 * @property id уникальный идентификатор доски
 * @property projectId идентификатор проекта, к которому относится доска
 * @property name название доски
 * @property position позиция доски внутри проекта (чем меньше, тем левее)
 * @property createdAt дата и время создания
 */
data class Board(
    val id: BoardId,
    val projectId: ProjectId,
    val name: String,
    val position: Int,
    val createdAt: Instant,
)
