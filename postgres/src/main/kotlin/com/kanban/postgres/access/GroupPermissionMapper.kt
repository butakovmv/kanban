package com.kanban.postgres.access

import com.kanban.access.GroupPermission
import com.kanban.common.GroupId
import com.kanban.common.PermissionId
import io.r2dbc.spi.Row
import java.time.ZoneId

internal fun Row.toGroupPermission(): GroupPermission {
    val table =
        GroupPermissionTable(
            groupId = get("group_id", String::class.java)!!,
            permissionId = get("permission_id", String::class.java)!!,
            grantedAt = get("granted_at", java.time.LocalDateTime::class.java)!!,
        )
    return table.toGroupPermission()
}

internal fun GroupPermission.toGroupPermissionTable(): GroupPermissionTable =
    GroupPermissionTable(
        groupId = groupId.value,
        permissionId = permissionId.value,
        grantedAt = grantedAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
    )

internal fun GroupPermissionTable.toGroupPermission(): GroupPermission =
    GroupPermission(
        groupId = GroupId(groupId),
        permissionId = PermissionId(permissionId),
        grantedAt = grantedAt.atZone(ZoneId.systemDefault()).toInstant(),
    )
