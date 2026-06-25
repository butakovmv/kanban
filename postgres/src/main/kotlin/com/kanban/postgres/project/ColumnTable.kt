package com.kanban.postgres.project

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
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
    @Column("project_id")
    val projectId: String,
    @Column("name")
    val name: String,
    @Column("position")
    val position: Int,
    @Column("wip_limit")
    val wipLimit: Int?,
    @Column("created_at")
    val createdAt: java.time.LocalDateTime,
)
