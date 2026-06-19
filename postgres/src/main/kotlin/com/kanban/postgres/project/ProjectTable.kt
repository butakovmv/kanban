package com.kanban.postgres.project

import org.springframework.data.annotation.Id
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
    val ownerId: String,
    val name: String,
    val description: String?,
    val createdAt: java.time.LocalDateTime,
    val updatedAt: java.time.LocalDateTime,
)
