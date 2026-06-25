package com.kanban.postgres.project

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

/**
 * Таблица `projects` — хранит проекты пользователей.
 * Каждый проект принадлежит владельцу (owner), имеет название и опциональное описание,
 * а также метки времени создания и последнего обновления.
 */
@Table("projects")
internal data class ProjectTable(
    @Id
    val id: String,
    @Column("owner_id")
    val ownerId: String,
    @Column("name")
    val name: String,
    @Column("description")
    val description: String?,
    @Column("created_at")
    val createdAt: java.time.LocalDateTime,
    @Column("updated_at")
    val updatedAt: java.time.LocalDateTime,
)
