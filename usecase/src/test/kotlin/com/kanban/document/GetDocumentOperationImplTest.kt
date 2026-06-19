package com.kanban.document

import com.kanban.common.DocumentId
import com.kanban.common.ProjectId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GetDocumentOperationImplTest {
    private val documentRepository = mockk<DocumentRepository>()
    private val operation = GetDocumentOperationImpl(documentRepository)

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
    fun `should return document when found`() =
        runTest {
            coEvery { documentRepository.findById("doc-1") } returns sampleDocument

            val result = operation.execute(GetDocumentOperation.Arg(documentId = "doc-1"))

            val success = assertIs<GetDocumentOperation.Result.Success>(result)
            assertEquals(sampleDocument, success.document)

            coVerify { documentRepository.findById("doc-1") }
        }

    @Test
    fun `should return NotFound when document not found`() =
        runTest {
            coEvery { documentRepository.findById("missing") } returns null

            val result = operation.execute(GetDocumentOperation.Arg(documentId = "missing"))

            assertIs<GetDocumentOperation.Result.NotFound>(result)

            coVerify { documentRepository.findById("missing") }
        }
}
