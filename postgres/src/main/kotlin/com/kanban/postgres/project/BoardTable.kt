package com.kanban.postgres.project

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

/**
 * Таблица `boards` — хранит доски проектов.
 * Каждая доска принадлежит проекту, имеет название, позицию для упорядочивания,
 * флаг архивации и метку времени создания.
 */
@Table("boards")
internal data class BoardTable(
    @Id
    val id: String,
    val projectId: String,
    val name: String,
    val position: Int,
    val archived: Boolean,
    val createdAt: java.time.LocalDateTime,
)
