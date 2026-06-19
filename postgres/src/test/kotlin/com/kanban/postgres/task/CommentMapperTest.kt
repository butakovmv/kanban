package com.kanban.postgres.task

import com.kanban.common.CommentId
import com.kanban.common.TaskId
import com.kanban.task.Comment
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

internal class CommentMapperTest {
    @Test
    fun `should map CommentTable to Comment domain`() {
        val now = LocalDateTime.now()
        val table =
            CommentTable(
                id = "comment-id",
                taskId = "task-id",
                authorId = "user-id",
                text = "Hello",
                createdAt = now,
                updatedAt = now,
            )

        val domain = table.toDomain()

        assertEquals(CommentId("comment-id"), domain.id)
        assertEquals(TaskId("task-id"), domain.taskId)
        assertEquals("user-id", domain.authorId)
        assertEquals("Hello", domain.text)
        assertEquals(now.atZone(ZoneId.systemDefault()).toInstant(), domain.createdAt)
        assertEquals(now.atZone(ZoneId.systemDefault()).toInstant(), domain.updatedAt)
    }

    @Test
    fun `should map Comment domain to CommentTable`() {
        val now = Instant.now()
        val domain =
            Comment(
                id = CommentId("comment-id"),
                taskId = TaskId("task-id"),
                authorId = "user-id",
                text = "World",
                createdAt = now,
                updatedAt = now,
            )

        val table = domain.toTable()

        assertEquals("comment-id", table.id)
        assertEquals("task-id", table.taskId)
        assertEquals("user-id", table.authorId)
        assertEquals("World", table.text)
        assertEquals(now.atZone(ZoneId.systemDefault()).toLocalDateTime(), table.createdAt)
        assertEquals(now.atZone(ZoneId.systemDefault()).toLocalDateTime(), table.updatedAt)
    }

    @Test
    fun `should roundtrip Comment domain through table`() {
        val original =
            Comment(
                id = CommentId("roundtrip-id"),
                taskId = TaskId("task-id"),
                authorId = "user",
                text = "rt",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )

        val table = original.toTable()
        val restored = table.toDomain()

        assertEquals(original.id, restored.id)
        assertEquals(original.taskId, restored.taskId)
        assertEquals(original.authorId, restored.authorId)
        assertEquals(original.text, restored.text)
        assertEquals(original.createdAt, restored.createdAt)
        assertEquals(original.updatedAt, restored.updatedAt)
    }
}
