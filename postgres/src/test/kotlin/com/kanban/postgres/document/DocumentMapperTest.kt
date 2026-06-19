package com.kanban.postgres.document

import com.kanban.common.DocumentId
import com.kanban.common.ProjectId
import com.kanban.document.Document
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.jupiter.api.Test

internal class DocumentMapperTest {
    @Test
    fun `should map DocumentTable to Document domain`() {
        val now = LocalDateTime.now()
        val table =
            DocumentTable(
                id = "doc-id",
                projectId = "project-id",
                title = "Specification",
                description = "Project spec",
                fileName = "spec.pdf",
                contentType = "application/pdf",
                sizeBytes = 4096L,
                storageKey = "projects/abc/doc-id/spec.pdf",
                version = 2,
                uploadedBy = "user-id",
                createdAt = now,
                updatedAt = now,
            )

        val domain = table.toDomain()

        assertEquals(DocumentId("doc-id"), domain.id)
        assertEquals(ProjectId("project-id"), domain.projectId)
        assertEquals("Specification", domain.title)
        assertEquals("Project spec", domain.description)
        assertEquals("spec.pdf", domain.fileName)
        assertEquals("application/pdf", domain.contentType)
        assertEquals(4096L, domain.sizeBytes)
        assertEquals("projects/abc/doc-id/spec.pdf", domain.storageKey)
        assertEquals(2, domain.version)
        assertEquals("user-id", domain.uploadedBy)
        assertEquals(now.atZone(ZoneId.systemDefault()).toInstant(), domain.createdAt)
        assertEquals(now.atZone(ZoneId.systemDefault()).toInstant(), domain.updatedAt)
    }

    @Test
    fun `should map DocumentTable with null description`() {
        val now = LocalDateTime.now()
        val table =
            DocumentTable(
                id = "doc-id",
                projectId = "project-id",
                title = "Specification",
                description = null,
                fileName = "spec.pdf",
                contentType = "application/pdf",
                sizeBytes = 4096L,
                storageKey = "projects/abc/spec.pdf",
                version = 1,
                uploadedBy = "user-id",
                createdAt = now,
                updatedAt = now,
            )

        val domain = table.toDomain()

        assertNull(domain.description)
    }

    @Test
    fun `should map Document domain to DocumentTable`() {
        val now = Instant.now()
        val domain =
            Document(
                id = DocumentId("doc-id"),
                projectId = ProjectId("project-id"),
                title = "Report",
                description = "Quarterly report",
                fileName = "report.docx",
                contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                sizeBytes = 8192L,
                storageKey = "projects/xyz/report.docx",
                version = 3,
                uploadedBy = "user",
                createdAt = now,
                updatedAt = now,
            )

        val table = domain.toTable()

        assertEquals("doc-id", table.id)
        assertEquals("project-id", table.projectId)
        assertEquals("Report", table.title)
        assertEquals("Quarterly report", table.description)
        assertEquals("report.docx", table.fileName)
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document", table.contentType)
        assertEquals(8192L, table.sizeBytes)
        assertEquals("projects/xyz/report.docx", table.storageKey)
        assertEquals(3, table.version)
        assertEquals("user", table.uploadedBy)
        assertEquals(now.atZone(ZoneId.systemDefault()).toLocalDateTime(), table.createdAt)
        assertEquals(now.atZone(ZoneId.systemDefault()).toLocalDateTime(), table.updatedAt)
    }

    @Test
    fun `should map Document domain to DocumentTable with null description`() {
        val now = Instant.now()
        val domain =
            Document(
                id = DocumentId("doc-id"),
                projectId = ProjectId("project-id"),
                title = "Report",
                description = null,
                fileName = "report.docx",
                contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                sizeBytes = 8192L,
                storageKey = "projects/xyz/report.docx",
                version = 1,
                uploadedBy = "user",
                createdAt = now,
                updatedAt = now,
            )

        val table = domain.toTable()

        assertNull(table.description)
    }

    @Test
    fun `should roundtrip Document domain through table`() {
        val original =
            Document(
                id = DocumentId("rt-id"),
                projectId = ProjectId("project-id"),
                title = "Roundtrip",
                description = "desc",
                fileName = "rt.bin",
                contentType = "application/octet-stream",
                sizeBytes = 4096L,
                storageKey = "projects/rt/rt.bin",
                version = 5,
                uploadedBy = "user",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )

        val table = original.toTable()
        val restored = table.toDomain()

        assertEquals(original.id, restored.id)
        assertEquals(original.projectId, restored.projectId)
        assertEquals(original.title, restored.title)
        assertEquals(original.description, restored.description)
        assertEquals(original.fileName, restored.fileName)
        assertEquals(original.contentType, restored.contentType)
        assertEquals(original.sizeBytes, restored.sizeBytes)
        assertEquals(original.storageKey, restored.storageKey)
        assertEquals(original.version, restored.version)
        assertEquals(original.uploadedBy, restored.uploadedBy)
        assertEquals(original.createdAt, restored.createdAt)
        assertEquals(original.updatedAt, restored.updatedAt)
    }
}
