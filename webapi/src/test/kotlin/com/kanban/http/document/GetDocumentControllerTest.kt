package com.kanban.http.document

import com.kanban.common.DocumentId
import com.kanban.common.ProjectId
import com.kanban.document.Document
import com.kanban.document.GetDocumentOperation
import io.mockk.coEvery
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера получения документа.
 * Проверяют корректность кодов ответа и тел ответов.
 */
internal class GetDocumentControllerTest : BaseDocumentControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(GetDocumentController::class.java)
    }

    @Test
    fun `should return document and 200`() {
        val documentId = "doc-${java.util.UUID.randomUUID()}"
        val now = Instant.now()
        val document =
            Document(
                id = DocumentId(documentId),
                projectId = ProjectId("project-1"),
                title = "Some title",
                description = "Some description",
                fileName = "file.pdf",
                contentType = "application/pdf",
                sizeBytes = 1024L,
                storageKey = "projects/project-1/documents/$documentId/file.pdf",
                version = 1,
                uploadedBy = "user-1",
                createdAt = now,
                updatedAt = now,
            )

        coEvery {
            getDocumentOperation.execute(any())
        } returns GetDocumentOperation.Result.Success(document = document)

        webClient
            .get()
            .uri("/api/v1/documents/$documentId")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo(documentId)
            .jsonPath("$.project_id")
            .isEqualTo("project-1")
            .jsonPath("$.title")
            .isEqualTo("Some title")
            .jsonPath("$.description")
            .isEqualTo("Some description")
            .jsonPath("$.file_name")
            .isEqualTo("file.pdf")
            .jsonPath("$.version")
            .isEqualTo(1)
    }

    @Test
    fun `should return 404 when document not found`() {
        val documentId = "missing-doc"

        coEvery {
            getDocumentOperation.execute(any())
        } returns GetDocumentOperation.Result.NotFound

        webClient
            .get()
            .uri("/api/v1/documents/$documentId")
            .exchange()
            .expectStatus()
            .isNotFound
    }
}
