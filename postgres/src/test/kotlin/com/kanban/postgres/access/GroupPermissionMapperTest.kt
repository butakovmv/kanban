package com.kanban.postgres.access

import com.kanban.access.GroupPermission
import com.kanban.common.GroupId
import com.kanban.common.PermissionId
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

internal class GroupPermissionMapperTest {
    @Test
    fun `should map GroupPermissionTable to GroupPermission domain`() {
        val now = LocalDateTime.now()
        val table =
            GroupPermissionTable(
                groupId = "group-id",
                permissionId = "perm-id",
                grantedAt = now,
            )

        val domain = table.toGroupPermission()

        assertEquals(GroupId("group-id"), domain.groupId)
        assertEquals(PermissionId("perm-id"), domain.permissionId)
        assertEquals(now.atZone(ZoneId.systemDefault()).toInstant(), domain.grantedAt)
    }

    @Test
    fun `should map GroupPermission domain to GroupPermissionTable`() {
        val now = Instant.now()
        val domain =
            GroupPermission(
                groupId = GroupId("group-id"),
                permissionId = PermissionId("perm-id"),
                grantedAt = now,
            )

        val table = domain.toGroupPermissionTable()

        assertEquals("group-id", table.groupId)
        assertEquals("perm-id", table.permissionId)
        assertEquals(now.atZone(ZoneId.systemDefault()).toLocalDateTime(), table.grantedAt)
    }
}
