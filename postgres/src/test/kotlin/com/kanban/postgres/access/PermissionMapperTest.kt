package com.kanban.postgres.access

import com.kanban.access.Permission
import com.kanban.common.PermissionId
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.jupiter.api.Test

internal class PermissionMapperTest {
    @Test
    fun `should map PermissionTable to Permission domain`() {
        val now = LocalDateTime.now()
        val table =
            PermissionTable(
                id = "perm-id",
                resource = "project",
                action = "read",
                targetId = "target-1",
                createdAt = now,
            )

        val domain = table.toPermission()

        assertEquals(PermissionId("perm-id"), domain.id)
        assertEquals("project", domain.resource)
        assertEquals("read", domain.action)
        assertEquals("target-1", domain.targetId)
        assertEquals(now.atZone(ZoneId.systemDefault()).toInstant(), domain.createdAt)
    }

    @Test
    fun `should map PermissionTable with null targetId`() {
        val now = LocalDateTime.now()
        val table =
            PermissionTable(
                id = "perm-id",
                resource = "board",
                action = "admin",
                targetId = null,
                createdAt = now,
            )

        val domain = table.toPermission()

        assertEquals("board", domain.resource)
        assertEquals("admin", domain.action)
        assertNull(domain.targetId)
    }

    @Test
    fun `should map Permission domain to PermissionTable`() {
        val now = Instant.now()
        val domain =
            Permission(
                id = PermissionId("perm-id"),
                resource = "task",
                action = "write",
                targetId = "task-42",
                createdAt = now,
            )

        val table = domain.toPermissionTable()

        assertEquals("perm-id", table.id)
        assertEquals("task", table.resource)
        assertEquals("write", table.action)
        assertEquals("task-42", table.targetId)
        assertEquals(now.atZone(ZoneId.systemDefault()).toLocalDateTime(), table.createdAt)
    }

    @Test
    fun `should map Permission domain with null targetId to PermissionTable`() {
        val now = Instant.now()
        val domain =
            Permission(
                id = PermissionId("perm-id"),
                resource = "project",
                action = "read",
                targetId = null,
                createdAt = now,
            )

        val table = domain.toPermissionTable()

        assertNull(table.targetId)
    }
}
