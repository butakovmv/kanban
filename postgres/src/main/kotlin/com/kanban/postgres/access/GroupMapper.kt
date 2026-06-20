package com.kanban.postgres.access

import com.kanban.access.Group
import com.kanban.common.GroupId
import io.r2dbc.spi.Row
import java.time.ZoneId

internal fun Row.toGroup(): Group {
    val table =
        GroupTable(
            id = get("id", String::class.java)!!,
            name = get("name", String::class.java)!!,
            description = get("description", String::class.java),
            createdAt = get("created_at", java.time.LocalDateTime::class.java)!!,
        )
    return table.toGroup()
}

internal fun Group.toGroupTable(): GroupTable =
    GroupTable(
        id = id.value,
        name = name,
        description = description,
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
    )

internal fun GroupTable.toGroup(): Group =
    Group(
        id = GroupId(id),
        name = name,
        description = description,
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toInstant(),
    )
