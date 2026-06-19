package com.kanban.project

import com.kanban.common.ProjectId
import com.kanban.common.UserId
import java.time.Instant

/**
 * Сущность проекта — контейнера для досок с задачами.
 * Принадлежит одному владельцу (пользователю), содержит название и опциональное описание.
 *
 * @property id уникальный идентификатор проекта
 * @property ownerId идентификатор пользователя-владельца
 * @property name название проекта
 * @property description описание проекта (опционально)
 * @property createdAt дата и время создания
 * @property updatedAt дата и время последнего изменения
 */
data class Project(
    val id: ProjectId,
    val ownerId: UserId,
    val name: String,
    val description: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
