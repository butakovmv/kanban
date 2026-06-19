package com.kanban.http.document

import com.kanban.common.DocumentId
import com.kanban.common.ProjectId
import com.kanban.document.Document
import com.kanban.document.UpdateDocumentOperation
import io.mockk.coEvery
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера обновления документа.
 * Проверяют корректность кодов ответа и тел запросов/ответов.
 */
internal class UpdateDocumentControllerTest : BaseDocumentControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(UpdateDocumentController::class.java)
    }

    @Test
    fun `should update document and return 200`() {
        val documentId = "doc-${java.util.UUID.randomUUID()}"
        val body = RequestGenerator.updateDocumentBody()
        val now = Instant.now()
        val document =
            Document(
                id = DocumentId(documentId),
                projectId = ProjectId("project-1"),
                title = body.title!!,
                description = body.description,
                fileName = "file.pdf",
                contentType = "application/pdf",
                sizeBytes = 100L,
                storageKey = "k",
                version = 1,
                uploadedBy = "u1",
                createdAt = now,
                updatedAt = now,
            )

        coEvery {
            updateDocumentOperation.execute(any())
        } returns UpdateDocumentOperation.Result.Success(document = document)

        webClient
            .put()
            .uri("/api/v1/documents/$documentId")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo(documentId)
            .jsonPath("$.title")
            .isEqualTo(body.title)
            .jsonPath("$.description")
            .isEqualTo(body.description!!)
    }

    @Test
    fun `should return 404 when document not found`() {
        val documentId = "missing-doc"
        val body = RequestGenerator.updateDocumentBody()

        coEvery {
            updateDocumentOperation.execute(any())
        } returns UpdateDocumentOperation.Result.NotFound

        webClient
            .put()
            .uri("/api/v1/documents/$documentId")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `should return 400 on failure`() {
        val documentId = "doc-${java.util.UUID.randomUUID()}"
        val body = RequestGenerator.updateDocumentBody()

        coEvery {
            updateDocumentOperation.execute(any())
        } returns UpdateDocumentOperation.Result.Failure("Title must not be blank")

        webClient
            .put()
            .uri("/api/v1/documents/$documentId")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.reason")
            .isEqualTo("Title must not be blank")
    }
}
