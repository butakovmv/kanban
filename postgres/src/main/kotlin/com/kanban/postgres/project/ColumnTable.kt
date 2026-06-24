package com.kanban.postgres.project

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

/**
 * Таблица `columns` — хранит колонки проекта.
 * Каждая колонка принадлежит проекту, имеет название, позицию для упорядочивания,
 * опциональный WIP-лимит и метку времени создания.
 */
@Table("columns")
internal data class ColumnTable(
    @Id
    val id: String,
    val projectId: String,
    val name: String,
    val position: Int,
    val wipLimit: Int?,
    val createdAt: java.time.LocalDateTime,
)
