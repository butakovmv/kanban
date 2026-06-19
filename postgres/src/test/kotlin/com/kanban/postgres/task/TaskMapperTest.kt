package com.kanban.postgres.task

import com.kanban.common.BoardId
import com.kanban.common.ColumnId
import com.kanban.common.TaskId
import com.kanban.task.Task
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.jupiter.api.Test

internal class TaskMapperTest {
    @Test
    fun `should map TaskTable to Task domain`() {
        val now = LocalDateTime.now()
        val due = now.plusDays(3)
        val table =
            TaskTable(
                id = "task-id",
                boardId = "board-id",
                columnId = "column-id",
                title = "Implement feature",
                description = "Some details",
                assigneeId = "user-id",
                position = 2,
                dueDate = due,
                archived = false,
                createdAt = now,
                updatedAt = now,
            )

        val domain = table.toDomain()

        assertEquals(TaskId("task-id"), domain.id)
        assertEquals(BoardId("board-id"), domain.boardId)
        assertEquals(ColumnId("column-id"), domain.columnId)
        assertEquals("Implement feature", domain.title)
        assertEquals("Some details", domain.description)
        assertEquals("user-id", domain.assigneeId)
        assertEquals(2, domain.position)
        assertEquals(due.atZone(ZoneId.systemDefault()).toInstant(), domain.dueDate)
        assertEquals(false, domain.archived)
        assertEquals(now.atZone(ZoneId.systemDefault()).toInstant(), domain.createdAt)
        assertEquals(now.atZone(ZoneId.systemDefault()).toInstant(), domain.updatedAt)
    }

    @Test
    fun `should map TaskTable with null description, assignee and dueDate`() {
        val now = LocalDateTime.now()
        val table =
            TaskTable(
                id = "task-id",
                boardId = "board-id",
                columnId = "column-id",
                title = "Task",
                description = null,
                assigneeId = null,
                position = 0,
                dueDate = null,
                archived = true,
                createdAt = now,
                updatedAt = now,
            )

        val domain = table.toDomain()

        assertNull(domain.description)
        assertNull(domain.assigneeId)
        assertNull(domain.dueDate)
        assertEquals(true, domain.archived)
    }

    @Test
    fun `should map Task domain to TaskTable`() {
        val now = Instant.now()
        val due = now.plusSeconds(3600)
        val domain =
            Task(
                id = TaskId("task-id"),
                boardId = BoardId("board-id"),
                columnId = ColumnId("column-id"),
                title = "Refactor",
                description = "Refactor module",
                assigneeId = "user-id",
                position = 1,
                dueDate = due,
                archived = true,
                createdAt = now,
                updatedAt = now,
            )

        val table = domain.toTable()

        assertEquals("task-id", table.id)
        assertEquals("board-id", table.boardId)
        assertEquals("column-id", table.columnId)
        assertEquals("Refactor", table.title)
        assertEquals("Refactor module", table.description)
        assertEquals("user-id", table.assigneeId)
        assertEquals(1, table.position)
        assertEquals(due.atZone(ZoneId.systemDefault()).toLocalDateTime(), table.dueDate)
        assertEquals(true, table.archived)
        assertEquals(now.atZone(ZoneId.systemDefault()).toLocalDateTime(), table.createdAt)
        assertEquals(now.atZone(ZoneId.systemDefault()).toLocalDateTime(), table.updatedAt)
    }

    @Test
    fun `should map Task domain to TaskTable with null fields`() {
        val now = Instant.now()
        val domain =
            Task(
                id = TaskId("task-id"),
                boardId = BoardId("board-id"),
                columnId = ColumnId("column-id"),
                title = "Empty",
                description = null,
                assigneeId = null,
                position = 0,
                dueDate = null,
                archived = false,
                createdAt = now,
                updatedAt = now,
            )

        val table = domain.toTable()

        assertNull(table.description)
        assertNull(table.assigneeId)
        assertNull(table.dueDate)
    }

    @Test
    fun `should roundtrip Task domain through table`() {
        val original =
            Task(
                id = TaskId("roundtrip-id"),
                boardId = BoardId("board-id"),
                columnId = ColumnId("column-id"),
                title = "Roundtrip",
                description = "desc",
                assigneeId = "user",
                position = 5,
                dueDate = Instant.now().plusSeconds(60),
                archived = false,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )

        val table = original.toTable()
        val restored = table.toDomain()

        assertEquals(original.id, restored.id)
        assertEquals(original.boardId, restored.boardId)
        assertEquals(original.columnId, restored.columnId)
        assertEquals(original.title, restored.title)
        assertEquals(original.description, restored.description)
        assertEquals(original.assigneeId, restored.assigneeId)
        assertEquals(original.position, restored.position)
        assertEquals(original.dueDate, restored.dueDate)
        assertEquals(original.archived, restored.archived)
        assertEquals(original.createdAt, restored.createdAt)
        assertEquals(original.updatedAt, restored.updatedAt)
    }
}
