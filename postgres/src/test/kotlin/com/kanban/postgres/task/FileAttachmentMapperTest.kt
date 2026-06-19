package com.kanban.postgres.task

import com.kanban.common.FileAttachmentId
import com.kanban.common.TaskId
import com.kanban.task.FileAttachment
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

internal class FileAttachmentMapperTest {
    @Test
    fun `should map FileAttachmentTable to FileAttachment domain`() {
        val now = LocalDateTime.now()
        val table =
            FileAttachmentTable(
                id = "file-id",
                taskId = "task-id",
                fileName = "doc.pdf",
                contentType = "application/pdf",
                sizeBytes = 2048L,
                storageKey = "tasks/abc/doc.pdf",
                uploadedBy = "user-id",
                uploadedAt = now,
            )

        val domain = table.toDomain()

        assertEquals(FileAttachmentId("file-id"), domain.id)
        assertEquals(TaskId("task-id"), domain.taskId)
        assertEquals("doc.pdf", domain.fileName)
        assertEquals("application/pdf", domain.contentType)
        assertEquals(2048L, domain.sizeBytes)
        assertEquals("tasks/abc/doc.pdf", domain.storageKey)
        assertEquals("user-id", domain.uploadedBy)
        assertEquals(now.atZone(ZoneId.systemDefault()).toInstant(), domain.uploadedAt)
    }

    @Test
    fun `should map FileAttachment domain to FileAttachmentTable`() {
        val now = Instant.now()
        val domain =
            FileAttachment(
                id = FileAttachmentId("file-id"),
                taskId = TaskId("task-id"),
                fileName = "image.png",
                contentType = "image/png",
                sizeBytes = 8192L,
                storageKey = "tasks/xyz/image.png",
                uploadedBy = "user",
                uploadedAt = now,
            )

        val table = domain.toTable()

        assertEquals("file-id", table.id)
        assertEquals("task-id", table.taskId)
        assertEquals("image.png", table.fileName)
        assertEquals("image/png", table.contentType)
        assertEquals(8192L, table.sizeBytes)
        assertEquals("tasks/xyz/image.png", table.storageKey)
        assertEquals("user", table.uploadedBy)
        assertEquals(now.atZone(ZoneId.systemDefault()).toLocalDateTime(), table.uploadedAt)
    }

    @Test
    fun `should roundtrip FileAttachment domain through table`() {
        val original =
            FileAttachment(
                id = FileAttachmentId("rt-id"),
                taskId = TaskId("task-id"),
                fileName = "rt.bin",
                contentType = "application/octet-stream",
                sizeBytes = 4096L,
                storageKey = "tasks/rt/rt.bin",
                uploadedBy = "user",
                uploadedAt = Instant.now(),
            )

        val table = original.toTable()
        val restored = table.toDomain()

        assertEquals(original.id, restored.id)
        assertEquals(original.taskId, restored.taskId)
        assertEquals(original.fileName, restored.fileName)
        assertEquals(original.contentType, restored.contentType)
        assertEquals(original.sizeBytes, restored.sizeBytes)
        assertEquals(original.storageKey, restored.storageKey)
        assertEquals(original.uploadedBy, restored.uploadedBy)
        assertEquals(original.uploadedAt, restored.uploadedAt)
    }
}
