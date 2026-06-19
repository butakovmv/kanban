package com.kanban.document

import com.kanban.common.DocumentId
import com.kanban.common.ProjectId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DeleteDocumentOperationImplTest {
    private val documentRepository = mockk<DocumentRepository>()
    private val documentStorage = mockk<DocumentStorage>()
    private val operation = DeleteDocumentOperationImpl(documentRepository, documentStorage)

    private val sampleDocument =
        Document(
            id = DocumentId("doc-1"),
            projectId = ProjectId("project-1"),
            title = "Spec",
            description = null,
            fileName = "spec.pdf",
            contentType = "application/pdf",
            sizeBytes = 10L,
            storageKey = "projects/project-1/documents/doc-1/spec.pdf",
            version = 1,
            uploadedBy = "user-1",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )

    @Test
    fun `should delete document from storage and repository`() =
        runTest {
            coEvery { documentRepository.findById("doc-1") } returns sampleDocument
            coEvery {
                documentStorage.delete("projects/project-1/documents/doc-1/spec.pdf")
            } returns Unit
            coEvery { documentRepository.delete("doc-1") } returns Unit

            val result = operation.execute(DeleteDocumentOperation.Arg(documentId = "doc-1"))

            assertIs<DeleteDocumentOperation.Result.Success>(result)

            coVerify { documentRepository.findById("doc-1") }
            coVerify { documentStorage.delete("projects/project-1/documents/doc-1/spec.pdf") }
            coVerify { documentRepository.delete("doc-1") }
        }

    @Test
    fun `should return NotFound when document not found`() =
        runTest {
            coEvery { documentRepository.findById("missing") } returns null

            val result = operation.execute(DeleteDocumentOperation.Arg(documentId = "missing"))

            assertIs<DeleteDocumentOperation.Result.NotFound>(result)

            coVerify { documentRepository.findById("missing") }
            coVerify(inverse = true) { documentStorage.delete(any()) }
            coVerify(inverse = true) { documentRepository.delete(any()) }
        }
}
