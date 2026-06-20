package com.kanban.postgres.access

import com.kanban.access.Permission
import com.kanban.common.PermissionId
import io.r2dbc.spi.Row
import java.time.ZoneId

internal fun Row.toPermission(): Permission {
    val table =
        PermissionTable(
            id = get("id", String::class.java)!!,
            resource = get("resource", String::class.java)!!,
            action = get("action", String::class.java)!!,
            targetId = get("target_id", String::class.java),
            createdAt = get("created_at", java.time.LocalDateTime::class.java)!!,
        )
    return table.toPermission()
}

internal fun Permission.toPermissionTable(): PermissionTable =
    PermissionTable(
        id = id.value,
        resource = resource,
        action = action,
        targetId = targetId,
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
    )

internal fun PermissionTable.toPermission(): Permission =
    Permission(
        id = PermissionId(id),
        resource = resource,
        action = action,
        targetId = targetId,
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toInstant(),
    )
