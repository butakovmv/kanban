package com.kanban.access

import com.kanban.common.PermissionId
import java.time.Instant

/**
 * Сущность разрешения на доступ к ресурсу.
 * Связывает тип ресурса (например, "project", "board", "task") с действием ("read", "write", "delete", "admin").
 * Разрешение может быть глобальным (targetId == null) или относиться к конкретному экземпляру ресурса.
 *
 * @property id уникальный идентификатор разрешения
 * @property resource тип ресурса, к которому относится разрешение
 * @property action действие, которое разрешено над ресурсом
 * @property targetId идентификатор конкретного экземпляра ресурса (null — глобальное разрешение)
 * @property createdAt дата и время создания
 */
data class Permission(
    val id: PermissionId,
    val resource: String,
    val action: String,
    val targetId: String?,
    val createdAt: Instant,
)
