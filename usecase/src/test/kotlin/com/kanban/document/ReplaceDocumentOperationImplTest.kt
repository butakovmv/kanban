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

class ReplaceDocumentOperationImplTest {
    private val documentRepository = mockk<DocumentRepository>()
    private val documentStorage = mockk<DocumentStorage>()
    private val operation = ReplaceDocumentOperationImpl(documentRepository, documentStorage)

    private val sampleDocument =
        Document(
            id = DocumentId("doc-1"),
            projectId = ProjectId("project-1"),
            title = "Spec",
            description = "desc",
            fileName = "spec.pdf",
            contentType = "application/pdf",
            sizeBytes = 10L,
            storageKey = "projects/project-1/documents/doc-1/spec.pdf",
            version = 2,
            uploadedBy = "user-1",
            createdAt = Instant.parse("2024-12-01T00:00:00Z"),
            updatedAt = Instant.parse("2024-12-01T00:00:00Z"),
        )

    @Test
    fun `should replace content and increment version`() =
        runTest {
            val newContent = "new body".toByteArray()
            coEvery { documentRepository.findById("doc-1") } returns sampleDocument
            coEvery { documentStorage.upload(any(), any(), any()) } answers { firstArg() }
            coEvery { documentStorage.delete(any()) } returns Unit
            coEvery { documentRepository.save(any()) } answers { firstArg() }

            val result =
                operation.execute(
                    ReplaceDocumentOperation.Arg(
                        documentId = "doc-1",
                        content = newContent,
                        newFileName = null,
                        newContentType = null,
                    ),
                )

            val success = assertIs<ReplaceDocumentOperation.Result.Success>(result)
            assertEquals(newContent.size.toLong(), success.document.sizeBytes)
            assertEquals(3, success.document.version)
            assertEquals(sampleDocument.fileName, success.document.fileName)
            assertEquals(sampleDocument.contentType, success.document.contentType)
            assertNotEquals(sampleDocument.updatedAt, success.document.updatedAt)

            coVerify { documentRepository.findById("doc-1") }
            coVerify {
                documentStorage.upload(
                    key =
                        match {
                            it.startsWith("projects/project-1/documents/doc-1/") && it.endsWith("spec.pdf")
                        },
                    content = newContent,
                    contentType = "application/pdf",
                )
            }
            coVerify { documentStorage.delete("projects/project-1/documents/doc-1/spec.pdf") }
            coVerify { documentRepository.save(any()) }
        }

    @Test
    fun `should update file name and content type when provided`() =
        runTest {
            val newContent = "data".toByteArray()
            coEvery { documentRepository.findById("doc-1") } returns sampleDocument
            coEvery { documentStorage.upload(any(), any(), any()) } answers { firstArg() }
            coEvery { documentStorage.delete(any()) } returns Unit
            coEvery { documentRepository.save(any()) } answers { firstArg() }

            val result =
                operation.execute(
                    ReplaceDocumentOperation.Arg(
                        documentId = "doc-1",
                        content = newContent,
                        newFileName = "v2.docx",
                        newContentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    ),
                )

            val success = assertIs<ReplaceDocumentOperation.Result.Success>(result)
            assertEquals("v2.docx", success.document.fileName)
            assertEquals(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                success.document.contentType,
            )
            assertEquals(3, success.document.version)

            coVerify {
                documentStorage.upload(
                    key = match { it.endsWith("v2.docx") },
                    content = newContent,
                    contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                )
            }
        }

    @Test
    fun `should return NotFound when document not found`() =
        runTest {
            coEvery { documentRepository.findById("missing") } returns null

            val result =
                operation.execute(
                    ReplaceDocumentOperation.Arg(
                        documentId = "missing",
                        content = "x".toByteArray(),
                        newFileName = null,
                        newContentType = null,
                    ),
                )

            assertIs<ReplaceDocumentOperation.Result.NotFound>(result)

            coVerify { documentRepository.findById("missing") }
            coVerify(inverse = true) { documentStorage.upload(any(), any(), any()) }
            coVerify(inverse = true) { documentStorage.delete(any()) }
            coVerify(inverse = true) { documentRepository.save(any()) }
        }

    @Test
    fun `should fail when content is empty`() =
        runTest {
            coEvery { documentRepository.findById("doc-1") } returns sampleDocument

            val result =
                operation.execute(
                    ReplaceDocumentOperation.Arg(
                        documentId = "doc-1",
                        content = ByteArray(0),
                        newFileName = null,
                        newContentType = null,
                    ),
                )

            val failure = assertIs<ReplaceDocumentOperation.Result.Failure>(result)
            assertEquals("Content must not be empty", failure.reason)

            coVerify { documentRepository.findById("doc-1") }
            coVerify(inverse = true) { documentStorage.upload(any(), any(), any()) }
            coVerify(inverse = true) { documentStorage.delete(any()) }
        }

    @Test
    fun `should fail when new file name is blank`() =
        runTest {
            coEvery { documentRepository.findById("doc-1") } returns sampleDocument

            val result =
                operation.execute(
                    ReplaceDocumentOperation.Arg(
                        documentId = "doc-1",
                        content = "x".toByteArray(),
                        newFileName = "  ",
                        newContentType = null,
                    ),
                )

            val failure = assertIs<ReplaceDocumentOperation.Result.Failure>(result)
            assertEquals("File name must not be blank", failure.reason)
        }

    @Test
    fun `should fail when new content type is blank`() =
        runTest {
            coEvery { documentRepository.findById("doc-1") } returns sampleDocument

            val result =
                operation.execute(
                    ReplaceDocumentOperation.Arg(
                        documentId = "doc-1",
                        content = "x".toByteArray(),
                        newFileName = null,
                        newContentType = "  ",
                    ),
                )

            val failure = assertIs<ReplaceDocumentOperation.Result.Failure>(result)
            assertEquals("Content type must not be blank", failure.reason)
        }
}
