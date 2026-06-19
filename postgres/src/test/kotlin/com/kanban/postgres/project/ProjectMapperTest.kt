package com.kanban.postgres.project

import com.kanban.common.ProjectId
import com.kanban.common.UserId
import com.kanban.project.Project
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.jupiter.api.Test

internal class ProjectMapperTest {
    @Test
    fun `should map ProjectTable to domain Project`() {
        val now = LocalDateTime.now()
        val table =
            ProjectTable(
                id = "project-id",
                ownerId = "owner-id",
                name = "Test Project",
                description = "Some description",
                createdAt = now,
                updatedAt = now,
            )

        val domain = table.toDomain()

        assertEquals(ProjectId("project-id"), domain.id)
        assertEquals(UserId("owner-id"), domain.ownerId)
        assertEquals("Test Project", domain.name)
        assertEquals("Some description", domain.description)
        assertEquals(now.atZone(ZoneId.systemDefault()).toInstant(), domain.createdAt)
        assertEquals(now.atZone(ZoneId.systemDefault()).toInstant(), domain.updatedAt)
    }

    @Test
    fun `should map domain Project to ProjectTable`() {
        val now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()
        val project =
            Project(
                id = ProjectId("project-id"),
                ownerId = UserId("owner-id"),
                name = "Test Project",
                description = null,
                createdAt = now,
                updatedAt = now,
            )

        val table = project.toTable()

        assertEquals("project-id", table.id)
        assertEquals("owner-id", table.ownerId)
        assertEquals("Test Project", table.name)
        assertNull(table.description)
    }
}
