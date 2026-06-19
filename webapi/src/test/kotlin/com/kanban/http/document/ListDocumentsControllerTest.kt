package com.kanban.http.document

import com.kanban.common.DocumentId
import com.kanban.common.ProjectId
import com.kanban.document.Document
import com.kanban.document.ListDocumentsOperation
import io.mockk.coEvery
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера получения списка документов проекта.
 * Проверяют корректность кодов ответа и тел ответов.
 */
internal class ListDocumentsControllerTest : BaseDocumentControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(ListDocumentsController::class.java)
    }

    @Test
    fun `should return documents list and 200`() {
        val projectId = "project-${java.util.UUID.randomUUID()}"
        val now = Instant.now()
        val documents =
            listOf(
                Document(
                    id = DocumentId("doc-1"),
                    projectId = ProjectId(projectId),
                    title = "Doc 1",
                    description = null,
                    fileName = "a.pdf",
                    contentType = "application/pdf",
                    sizeBytes = 10L,
                    storageKey = "k1",
                    version = 1,
                    uploadedBy = "u1",
                    createdAt = now,
                    updatedAt = now,
                ),
                Document(
                    id = DocumentId("doc-2"),
                    projectId = ProjectId(projectId),
                    title = "Doc 2",
                    description = "d",
                    fileName = "b.pdf",
                    contentType = "application/pdf",
                    sizeBytes = 20L,
                    storageKey = "k2",
                    version = 1,
                    uploadedBy = "u2",
                    createdAt = now,
                    updatedAt = now,
                ),
            )

        coEvery {
            listDocumentsOperation.execute(any())
        } returns ListDocumentsOperation.Result.Success(documents = documents)

        webClient
            .get()
            .uri("/api/v1/projects/$projectId/documents")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.documents.length()")
            .isEqualTo(2)
            .jsonPath("$.documents[0].id")
            .isEqualTo("doc-1")
            .jsonPath("$.documents[0].title")
            .isEqualTo("Doc 1")
            .jsonPath("$.documents[1].id")
            .isEqualTo("doc-2")
            .jsonPath("$.documents[1].title")
            .isEqualTo("Doc 2")
    }

    @Test
    fun `should return empty list and 200`() {
        val projectId = "project-${java.util.UUID.randomUUID()}"

        coEvery {
            listDocumentsOperation.execute(any())
        } returns ListDocumentsOperation.Result.Success(documents = emptyList())

        webClient
            .get()
            .uri("/api/v1/projects/$projectId/documents")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.documents.length()")
            .isEqualTo(0)
    }
}
