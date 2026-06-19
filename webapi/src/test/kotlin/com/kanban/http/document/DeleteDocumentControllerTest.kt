package com.kanban.http.document

import com.kanban.document.DeleteDocumentOperation
import io.mockk.coEvery
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера удаления документа.
 * Проверяют корректность кодов ответа.
 */
internal class DeleteDocumentControllerTest : BaseDocumentControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(DeleteDocumentController::class.java)
    }

    @Test
    fun `should delete document and return 204`() {
        val documentId = "doc-${java.util.UUID.randomUUID()}"

        coEvery {
            deleteDocumentOperation.execute(any())
        } returns DeleteDocumentOperation.Result.Success

        webClient
            .delete()
            .uri("/api/v1/documents/$documentId")
            .exchange()
            .expectStatus()
            .isNoContent
    }

    @Test
    fun `should return 404 when document not found`() {
        val documentId = "missing-doc"

        coEvery {
            deleteDocumentOperation.execute(any())
        } returns DeleteDocumentOperation.Result.NotFound

        webClient
            .delete()
            .uri("/api/v1/documents/$documentId")
            .exchange()
            .expectStatus()
            .isNotFound
    }
}
