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

class ListDocumentsOperationImplTest {
    private val documentRepository = mockk<DocumentRepository>()
    private val operation = ListDocumentsOperationImpl(documentRepository)

    @Test
    fun `should return documents for project`() =
        runTest {
            val documents =
                listOf(
                    Document(
                        id = DocumentId("doc-1"),
                        projectId = ProjectId("project-1"),
                        title = "A",
                        description = null,
                        fileName = "a.txt",
                        contentType = "text/plain",
                        sizeBytes = 1L,
                        storageKey = "k-1",
                        version = 1,
                        uploadedBy = "user-1",
                        createdAt = Instant.now(),
                        updatedAt = Instant.now(),
                    ),
                    Document(
                        id = DocumentId("doc-2"),
                        projectId = ProjectId("project-1"),
                        title = "B",
                        description = null,
                        fileName = "b.txt",
                        contentType = "text/plain",
                        sizeBytes = 2L,
                        storageKey = "k-2",
                        version = 1,
                        uploadedBy = "user-2",
                        createdAt = Instant.now(),
                        updatedAt = Instant.now(),
                    ),
                )
            coEvery { documentRepository.listByProjectId("project-1") } returns documents

            val result = operation.execute(ListDocumentsOperation.Arg(projectId = "project-1"))

            val success = assertIs<ListDocumentsOperation.Result.Success>(result)
            assertEquals(documents, success.documents)

            coVerify { documentRepository.listByProjectId("project-1") }
        }

    @Test
    fun `should return empty list when project has no documents`() =
        runTest {
            coEvery { documentRepository.listByProjectId("empty") } returns emptyList()

            val result = operation.execute(ListDocumentsOperation.Arg(projectId = "empty"))

            val success = assertIs<ListDocumentsOperation.Result.Success>(result)
            assertEquals(emptyList(), success.documents)
        }
}
