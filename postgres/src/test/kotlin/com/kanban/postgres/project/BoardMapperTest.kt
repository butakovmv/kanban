package com.kanban.postgres.project

import com.kanban.common.BoardId
import com.kanban.common.ProjectId
import com.kanban.project.Board
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

internal class BoardMapperTest {
    @Test
    fun `should map BoardTable to domain Board ignoring archived flag`() {
        val now = LocalDateTime.now()
        val table =
            BoardTable(
                id = "board-id",
                projectId = "project-id",
                name = "Main Board",
                position = 3,
                archived = true,
                createdAt = now,
            )

        val domain = table.toDomain()

        assertEquals(BoardId("board-id"), domain.id)
        assertEquals(ProjectId("project-id"), domain.projectId)
        assertEquals("Main Board", domain.name)
        assertEquals(3, domain.position)
        assertEquals(now.atZone(ZoneId.systemDefault()).toInstant(), domain.createdAt)
    }

    @Test
    fun `should map domain Board to BoardTable with archived=false`() {
        val now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()
        val board =
            Board(
                id = BoardId("board-id"),
                projectId = ProjectId("project-id"),
                name = "Main Board",
                position = 2,
                createdAt = now,
            )

        val table = board.toTable()

        assertEquals("board-id", table.id)
        assertEquals("project-id", table.projectId)
        assertEquals("Main Board", table.name)
        assertEquals(2, table.position)
        assertEquals(false, table.archived)
    }
}
