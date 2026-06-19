package com.kanban.http.task

import com.kanban.task.GetFileDownloadUrlOperation
import io.mockk.coEvery
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера получения URL для скачивания файла.
 * Проверяют корректность кодов ответа и тел ответов.
 */
internal class GetFileDownloadUrlControllerTest : BaseTaskControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(GetFileDownloadUrlController::class.java)
    }

    @Test
    fun `should return 200 with url when file found`() {
        val id = "file-${UUID.randomUUID()}"
        val expectedUrl = "https://storage.example.com/presigned/${UUID.randomUUID()}"

        coEvery {
            getFileDownloadUrlOperation.execute(any())
        } returns GetFileDownloadUrlOperation.Result.Success(url = expectedUrl)

        webClient
            .get()
            .uri("/api/v1/files/$id/download")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.url")
            .isEqualTo(expectedUrl)
    }

    @Test
    fun `should return 404 when file not found`() {
        val id = "missing-${UUID.randomUUID()}"

        coEvery {
            getFileDownloadUrlOperation.execute(any())
        } returns GetFileDownloadUrlOperation.Result.NotFound

        webClient
            .get()
            .uri("/api/v1/files/$id/download")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound
    }
}
