package com.kanban.postgres.access

import com.kanban.access.GroupMember
import com.kanban.common.GroupId
import io.r2dbc.spi.Row
import java.time.ZoneId

internal fun Row.toGroupMember(): GroupMember {
    val table =
        GroupMemberTable(
            groupId = get("group_id", String::class.java)!!,
            userId = get("user_id", String::class.java)!!,
            addedAt = get("added_at", java.time.LocalDateTime::class.java)!!,
        )
    return table.toGroupMember()
}

internal fun GroupMember.toGroupMemberTable(): GroupMemberTable =
    GroupMemberTable(
        groupId = groupId.value,
        userId = userId,
        addedAt = addedAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
    )

internal fun GroupMemberTable.toGroupMember(): GroupMember =
    GroupMember(
        groupId = GroupId(groupId),
        userId = userId,
        addedAt = addedAt.atZone(ZoneId.systemDefault()).toInstant(),
    )
