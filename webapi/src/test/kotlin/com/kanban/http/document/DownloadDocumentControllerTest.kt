package com.kanban.http.document

import com.kanban.common.DocumentId
import com.kanban.common.ProjectId
import com.kanban.document.Document
import com.kanban.document.GetDocumentOperation
import io.mockk.coEvery
import java.time.Instant
import kotlin.time.Duration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера получения presigned-URL для скачивания документа.
 * Проверяют корректность кодов ответа и тел ответов.
 */
internal class DownloadDocumentControllerTest : BaseDocumentControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(DownloadDocumentController::class.java)
    }

    @Test
    fun `should return presigned url and 200`() {
        val documentId = "doc-${java.util.UUID.randomUUID()}"
        val storageKey = "projects/project-1/documents/$documentId/file.pdf"
        val now = Instant.now()
        val document =
            Document(
                id = DocumentId(documentId),
                projectId = ProjectId("project-1"),
                title = "Title",
                description = null,
                fileName = "file.pdf",
                contentType = "application/pdf",
                sizeBytes = 100L,
                storageKey = storageKey,
                version = 1,
                uploadedBy = "u1",
                createdAt = now,
                updatedAt = now,
            )
        val expectedUrl = "https://minio.example.com/download/$storageKey?signature=abc"

        coEvery {
            getDocumentOperation.execute(any())
        } returns GetDocumentOperation.Result.Success(document = document)
        coEvery {
            documentStorage.getDownloadUrl(storageKey, any<Duration>())
        } returns expectedUrl

        webClient
            .get()
            .uri("/api/v1/documents/$documentId/download")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.url")
            .isEqualTo(expectedUrl)
    }

    @Test
    fun `should return 404 when document not found`() {
        val documentId = "missing-doc"

        coEvery {
            getDocumentOperation.execute(any())
        } returns GetDocumentOperation.Result.NotFound

        webClient
            .get()
            .uri("/api/v1/documents/$documentId/download")
            .exchange()
            .expectStatus()
            .isNotFound
    }
}
