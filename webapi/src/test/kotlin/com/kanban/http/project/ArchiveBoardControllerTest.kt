package com.kanban.http.project

import com.kanban.project.ArchiveBoardOperation
import io.mockk.coEvery
import io.mockk.coVerify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера архивирования доски.
 * Проверяют корректность кодов ответа.
 */
internal class ArchiveBoardControllerTest : BaseProjectControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(ArchiveBoardController::class.java)
    }

    @Test
    fun `should archive board and return 204`() {
        val id = "board-${java.util.UUID.randomUUID()}"

        coEvery {
            archiveBoardOperation.execute(any())
        } returns ArchiveBoardOperation.Result.Success

        webClient
            .post()
            .uri("/api/v1/boards/$id/archive")
            .exchange()
            .expectStatus()
            .isNoContent

        coVerify {
            archiveBoardOperation.execute(match { it.boardId == id })
        }
    }

    @Test
    fun `should return 404 when board not found`() {
        val id = "missing-${java.util.UUID.randomUUID()}"

        coEvery {
            archiveBoardOperation.execute(any())
        } returns ArchiveBoardOperation.Result.NotFound

        webClient
            .post()
            .uri("/api/v1/boards/$id/archive")
            .exchange()
            .expectStatus()
            .isNotFound
    }
}
