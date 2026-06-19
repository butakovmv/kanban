package com.kanban.postgres.project

import com.kanban.common.BoardId
import com.kanban.common.ColumnId
import com.kanban.project.Column
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.jupiter.api.Test

internal class ColumnMapperTest {
    @Test
    fun `should map ColumnTable to domain Column`() {
        val now = LocalDateTime.now()
        val table =
            ColumnTable(
                id = "column-id",
                boardId = "board-id",
                name = "To Do",
                position = 1,
                wipLimit = 7,
                createdAt = now,
            )

        val domain = table.toDomain()

        assertEquals(ColumnId("column-id"), domain.id)
        assertEquals(BoardId("board-id"), domain.boardId)
        assertEquals("To Do", domain.name)
        assertEquals(1, domain.position)
        assertEquals(7, domain.wipLimit)
        assertEquals(now.atZone(ZoneId.systemDefault()).toInstant(), domain.createdAt)
    }

    @Test
    fun `should map ColumnTable with null wip limit`() {
        val now = LocalDateTime.now()
        val table =
            ColumnTable(
                id = "column-id",
                boardId = "board-id",
                name = "Backlog",
                position = 0,
                wipLimit = null,
                createdAt = now,
            )

        val domain = table.toDomain()

        assertNull(domain.wipLimit)
    }

    @Test
    fun `should map domain Column to ColumnTable`() {
        val now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()
        val column =
            Column(
                id = ColumnId("column-id"),
                boardId = BoardId("board-id"),
                name = "To Do",
                position = 2,
                wipLimit = null,
                createdAt = now,
            )

        val table = column.toTable()

        assertEquals("column-id", table.id)
        assertEquals("board-id", table.boardId)
        assertEquals("To Do", table.name)
        assertEquals(2, table.position)
        assertNull(table.wipLimit)
    }
}
