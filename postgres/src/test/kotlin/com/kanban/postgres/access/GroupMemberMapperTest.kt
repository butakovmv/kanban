package com.kanban.postgres.access

import com.kanban.access.GroupMember
import com.kanban.common.GroupId
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

internal class GroupMemberMapperTest {
    @Test
    fun `should map GroupMemberTable to GroupMember domain`() {
        val now = LocalDateTime.now()
        val table =
            GroupMemberTable(
                groupId = "group-id",
                userId = "user-id",
                addedAt = now,
            )

        val domain = table.toGroupMember()

        assertEquals(GroupId("group-id"), domain.groupId)
        assertEquals("user-id", domain.userId)
        assertEquals(now.atZone(ZoneId.systemDefault()).toInstant(), domain.addedAt)
    }

    @Test
    fun `should map GroupMember domain to GroupMemberTable`() {
        val now = Instant.now()
        val domain =
            GroupMember(
                groupId = GroupId("group-id"),
                userId = "user-id",
                addedAt = now,
            )

        val table = domain.toGroupMemberTable()

        assertEquals("group-id", table.groupId)
        assertEquals("user-id", table.userId)
        assertEquals(now.atZone(ZoneId.systemDefault()).toLocalDateTime(), table.addedAt)
    }
}
