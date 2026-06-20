package com.kanban.access

import com.kanban.common.GroupId
import java.time.Instant

/**
 * Сущность группы пользователей.
 * Используется для объединения пользователей и назначения им набора разрешений.
 *
 * @property id уникальный идентификатор группы
 * @property name название группы
 * @property description описание группы (опционально)
 * @property createdAt дата и время создания
 */
data class Group(
    val id: GroupId,
    val name: String,
    val description: String?,
    val createdAt: Instant,
)
