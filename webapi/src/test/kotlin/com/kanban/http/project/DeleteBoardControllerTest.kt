package com.kanban.http.project

import com.kanban.project.DeleteBoardOperation
import io.mockk.coEvery
import io.mockk.coVerify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера удаления доски.
 * Проверяют корректность кодов ответа.
 */
internal class DeleteBoardControllerTest : BaseProjectControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(DeleteBoardController::class.java)
    }

    @Test
    fun `should delete board and return 204`() {
        val id = "board-${java.util.UUID.randomUUID()}"

        coEvery {
            deleteBoardOperation.execute(any())
        } returns DeleteBoardOperation.Result.Success

        webClient
            .delete()
            .uri("/api/v1/boards/$id")
            .exchange()
            .expectStatus()
            .isNoContent

        coVerify {
            deleteBoardOperation.execute(match { it.boardId == id })
        }
    }

    @Test
    fun `should return 404 when board not found`() {
        val id = "missing-${java.util.UUID.randomUUID()}"

        coEvery {
            deleteBoardOperation.execute(any())
        } returns DeleteBoardOperation.Result.NotFound

        webClient
            .delete()
            .uri("/api/v1/boards/$id")
            .exchange()
            .expectStatus()
            .isNotFound
    }
}
