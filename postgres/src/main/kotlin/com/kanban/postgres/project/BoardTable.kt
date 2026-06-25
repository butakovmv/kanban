package com.kanban.postgres.project

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
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
    @Column("project_id")
    val projectId: String,
    @Column("name")
    val name: String,
    @Column("position")
    val position: Int,
    @Column("archived")
    val archived: Boolean,
    @Column("created_at")
    val createdAt: java.time.LocalDateTime,
)
