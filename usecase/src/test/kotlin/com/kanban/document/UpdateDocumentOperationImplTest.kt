package com.kanban.document

import com.kanban.common.DocumentId
import com.kanban.common.ProjectId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class UpdateDocumentOperationImplTest {
    private val documentRepository = mockk<DocumentRepository>()
    private val operation = UpdateDocumentOperationImpl(documentRepository)

    private val sampleDocument =
        Document(
            id = DocumentId("doc-1"),
            projectId = ProjectId("project-1"),
            title = "Old Title",
            description = "Old Description",
            fileName = "doc.txt",
            contentType = "text/plain",
            sizeBytes = 10L,
            storageKey = "projects/project-1/documents/doc-1/doc.txt",
            version = 3,
            uploadedBy = "user-1",
            createdAt = Instant.parse("2024-12-01T00:00:00Z"),
            updatedAt = Instant.parse("2024-12-01T00:00:00Z"),
        )

    @Test
    fun `should update title and description without version change`() =
        runTest {
            coEvery { documentRepository.findById("doc-1") } returns sampleDocument
            coEvery { documentRepository.save(any()) } answers { firstArg() }

            val result =
                operation.execute(
                    UpdateDocumentOperation.Arg(
                        documentId = "doc-1",
                        title = "  New Title  ",
                        description = "New Description",
                    ),
                )

            val success = assertIs<UpdateDocumentOperation.Result.Success>(result)
            assertEquals("New Title", success.document.title)
            assertEquals("New Description", success.document.description)
            assertEquals(3, success.document.version)
            assertNotEquals(sampleDocument.updatedAt, success.document.updatedAt)

            coVerify { documentRepository.findById("doc-1") }
            coVerify { documentRepository.save(any()) }
        }

    @Test
    fun `should keep existing values when arguments are null`() =
        runTest {
            coEvery { documentRepository.findById("doc-1") } returns sampleDocument
            coEvery { documentRepository.save(any()) } answers { firstArg() }

            val result =
                operation.execute(
                    UpdateDocumentOperation.Arg(
                        documentId = "doc-1",
                        title = null,
                        description = null,
                    ),
                )

            val success = assertIs<UpdateDocumentOperation.Result.Success>(result)
            assertEquals(sampleDocument.title, success.document.title)
            assertEquals(sampleDocument.description, success.document.description)
            assertEquals(sampleDocument.version, success.document.version)
        }

    @Test
    fun `should update only title`() =
        runTest {
            coEvery { documentRepository.findById("doc-1") } returns sampleDocument
            coEvery { documentRepository.save(any()) } answers { firstArg() }

            val result =
                operation.execute(
                    UpdateDocumentOperation.Arg(
                        documentId = "doc-1",
                        title = "Only Title",
                        description = null,
                    ),
                )

            val success = assertIs<UpdateDocumentOperation.Result.Success>(result)
            assertEquals("Only Title", success.document.title)
            assertEquals(sampleDocument.description, success.document.description)
        }

    @Test
    fun `should return NotFound when document not found`() =
        runTest {
            coEvery { documentRepository.findById("missing") } returns null

            val result =
                operation.execute(
                    UpdateDocumentOperation.Arg(
                        documentId = "missing",
                        title = "x",
                        description = null,
                    ),
                )

            assertIs<UpdateDocumentOperation.Result.NotFound>(result)

            coVerify { documentRepository.findById("missing") }
            coVerify(inverse = true) { documentRepository.save(any()) }
        }

    @Test
    fun `should fail when title is blank`() =
        runTest {
            coEvery { documentRepository.findById("doc-1") } returns sampleDocument

            val result =
                operation.execute(
                    UpdateDocumentOperation.Arg(
                        documentId = "doc-1",
                        title = "   ",
                        description = null,
                    ),
                )

            val failure = assertIs<UpdateDocumentOperation.Result.Failure>(result)
            assertEquals("Title must not be blank", failure.reason)

            coVerify { documentRepository.findById("doc-1") }
            coVerify(inverse = true) { documentRepository.save(any()) }
        }
}
