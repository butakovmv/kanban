package com.kanban.http.document

import com.kanban.common.DocumentId
import com.kanban.common.ProjectId
import com.kanban.document.CreateDocumentOperation
import com.kanban.document.Document
import io.mockk.coEvery
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера создания документа.
 * Проверяют корректность кодов ответа и тел запросов/ответов.
 */
internal class CreateDocumentControllerTest : BaseDocumentControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(CreateDocumentController::class.java)
    }

    @Test
    fun `should create document and return 201`() {
        val body = RequestGenerator.createDocumentBody()
        val now = Instant.now()
        val document =
            Document(
                id = DocumentId("new-doc-id"),
                projectId = ProjectId(body.projectId),
                title = body.title,
                description = body.description,
                fileName = body.fileName,
                contentType = body.contentType,
                sizeBytes = 12L,
                storageKey = "projects/${body.projectId}/documents/new-doc-id/${body.fileName}",
                version = 1,
                uploadedBy = body.uploadedBy,
                createdAt = now,
                updatedAt = now,
            )

        coEvery {
            createDocumentOperation.execute(any())
        } returns CreateDocumentOperation.Result.Success(document = document)

        webClient
            .post()
            .uri("/api/v1/documents")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isCreated
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo("new-doc-id")
            .jsonPath("$.project_id")
            .isEqualTo(body.projectId)
            .jsonPath("$.title")
            .isEqualTo(body.title)
            .jsonPath("$.description")
            .isEqualTo(body.description!!)
            .jsonPath("$.file_name")
            .isEqualTo(body.fileName)
            .jsonPath("$.content_type")
            .isEqualTo(body.contentType)
            .jsonPath("$.uploaded_by")
            .isEqualTo(body.uploadedBy)
            .jsonPath("$.version")
            .isEqualTo(1)
    }

    @Test
    fun `should create document without description and return 201`() {
        val body = RequestGenerator.createDocumentBodyWithoutDescription()
        val now = Instant.now()
        val document =
            Document(
                id = DocumentId("new-doc-id"),
                projectId = ProjectId(body.projectId),
                title = body.title,
                description = null,
                fileName = body.fileName,
                contentType = body.contentType,
                sizeBytes = 12L,
                storageKey = "projects/${body.projectId}/documents/new-doc-id/${body.fileName}",
                version = 1,
                uploadedBy = body.uploadedBy,
                createdAt = now,
                updatedAt = now,
            )

        coEvery {
            createDocumentOperation.execute(any())
        } returns CreateDocumentOperation.Result.Success(document = document)

        webClient
            .post()
            .uri("/api/v1/documents")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isCreated
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo("new-doc-id")
            .jsonPath("$.description")
            .doesNotExist()
    }

    @Test
    fun `should return 400 on failure`() {
        val body = RequestGenerator.createDocumentBody()

        coEvery {
            createDocumentOperation.execute(any())
        } returns CreateDocumentOperation.Result.Failure("Project not found")

        webClient
            .post()
            .uri("/api/v1/documents")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.reason")
            .isEqualTo("Project not found")
    }
}
