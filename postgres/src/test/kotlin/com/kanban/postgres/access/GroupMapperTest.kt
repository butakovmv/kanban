package com.kanban.postgres.access

import com.kanban.access.Group
import com.kanban.common.GroupId
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.jupiter.api.Test

internal class GroupMapperTest {
    @Test
    fun `should map GroupTable to Group domain`() {
        val now = LocalDateTime.now()
        val table =
            GroupTable(
                id = "group-id",
                name = "Admins",
                description = "Administrator group",
                createdAt = now,
            )

        val domain = table.toGroup()

        assertEquals(GroupId("group-id"), domain.id)
        assertEquals("Admins", domain.name)
        assertEquals("Administrator group", domain.description)
        assertEquals(now.atZone(ZoneId.systemDefault()).toInstant(), domain.createdAt)
    }

    @Test
    fun `should map GroupTable with null description`() {
        val now = LocalDateTime.now()
        val table =
            GroupTable(
                id = "group-id",
                name = "Users",
                description = null,
                createdAt = now,
            )

        val domain = table.toGroup()

        assertEquals("Users", domain.name)
        assertNull(domain.description)
    }

    @Test
    fun `should map Group domain to GroupTable`() {
        val now = Instant.now()
        val domain =
            Group(
                id = GroupId("group-id"),
                name = "Managers",
                description = "Management team",
                createdAt = now,
            )

        val table = domain.toGroupTable()

        assertEquals("group-id", table.id)
        assertEquals("Managers", table.name)
        assertEquals("Management team", table.description)
        assertEquals(now.atZone(ZoneId.systemDefault()).toLocalDateTime(), table.createdAt)
    }

    @Test
    fun `should map Group domain with null description to GroupTable`() {
        val now = Instant.now()
        val domain =
            Group(
                id = GroupId("group-id"),
                name = "Viewers",
                description = null,
                createdAt = now,
            )

        val table = domain.toGroupTable()

        assertNull(table.description)
    }
}
