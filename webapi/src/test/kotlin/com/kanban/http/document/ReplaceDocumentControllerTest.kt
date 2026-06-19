package com.kanban.http.document

import com.kanban.common.DocumentId
import com.kanban.common.ProjectId
import com.kanban.document.Document
import com.kanban.document.ReplaceDocumentOperation
import io.mockk.coEvery
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера замены содержимого документа.
 * Проверяют корректность кодов ответа и тел запросов/ответов.
 */
internal class ReplaceDocumentControllerTest : BaseDocumentControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(ReplaceDocumentController::class.java)
    }

    @Test
    fun `should replace document content and return 200`() {
        val documentId = "doc-${java.util.UUID.randomUUID()}"
        val body = RequestGenerator.replaceDocumentBody()
        val now = Instant.now()
        val document =
            Document(
                id = DocumentId(documentId),
                projectId = ProjectId("project-1"),
                title = "Title",
                description = null,
                fileName = body.fileName!!,
                contentType = body.contentType!!,
                sizeBytes = 11L,
                storageKey = "k",
                version = 2,
                uploadedBy = "u1",
                createdAt = now,
                updatedAt = now,
            )

        coEvery {
            replaceDocumentOperation.execute(any())
        } returns ReplaceDocumentOperation.Result.Success(document = document)

        webClient
            .put()
            .uri("/api/v1/documents/$documentId/content")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo(documentId)
            .jsonPath("$.file_name")
            .isEqualTo(body.fileName)
            .jsonPath("$.version")
            .isEqualTo(2)
    }

    @Test
    fun `should replace content without optional fields and return 200`() {
        val documentId = "doc-${java.util.UUID.randomUUID()}"
        val body = RequestGenerator.replaceDocumentBodyWithoutOptionals()
        val now = Instant.now()
        val document =
            Document(
                id = DocumentId(documentId),
                projectId = ProjectId("project-1"),
                title = "Title",
                description = null,
                fileName = "existing.pdf",
                contentType = "application/pdf",
                sizeBytes = 11L,
                storageKey = "k",
                version = 2,
                uploadedBy = "u1",
                createdAt = now,
                updatedAt = now,
            )

        coEvery {
            replaceDocumentOperation.execute(any())
        } returns ReplaceDocumentOperation.Result.Success(document = document)

        webClient
            .put()
            .uri("/api/v1/documents/$documentId/content")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo(documentId)
            .jsonPath("$.version")
            .isEqualTo(2)
    }

    @Test
    fun `should return 404 when document not found`() {
        val documentId = "missing-doc"
        val body = RequestGenerator.replaceDocumentBody()

        coEvery {
            replaceDocumentOperation.execute(any())
        } returns ReplaceDocumentOperation.Result.NotFound

        webClient
            .put()
            .uri("/api/v1/documents/$documentId/content")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `should return 400 on failure`() {
        val documentId = "doc-${java.util.UUID.randomUUID()}"
        val body = RequestGenerator.replaceDocumentBody()

        coEvery {
            replaceDocumentOperation.execute(any())
        } returns ReplaceDocumentOperation.Result.Failure("Content must not be empty")

        webClient
            .put()
            .uri("/api/v1/documents/$documentId/content")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.reason")
            .isEqualTo("Content must not be empty")
    }
}
