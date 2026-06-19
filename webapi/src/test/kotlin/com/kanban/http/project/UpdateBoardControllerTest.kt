package com.kanban.http.project

import com.kanban.common.BoardId
import com.kanban.common.ProjectId
import com.kanban.project.Board
import com.kanban.project.UpdateBoardOperation
import io.mockk.coEvery
import io.mockk.coVerify
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера обновления доски.
 * Проверяют корректность кодов ответа и тел запросов/ответов.
 */
internal class UpdateBoardControllerTest : BaseProjectControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(UpdateBoardController::class.java)
    }

    @Test
    fun `should update board and return 200`() {
        val id = "board-${java.util.UUID.randomUUID()}"
        val body = RequestGenerator.updateBoardBody()
        val board =
            Board(
                id = BoardId(id),
                projectId = ProjectId("project-id"),
                name = body.name!!,
                position = 0,
                createdAt = Instant.now(),
            )

        coEvery {
            updateBoardOperation.execute(any())
        } returns UpdateBoardOperation.Result.Success(board = board)

        webClient
            .put()
            .uri("/api/v1/boards/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo(id)
            .jsonPath("$.name")
            .isEqualTo(body.name)

        coVerify {
            updateBoardOperation.execute(
                match { it.boardId == id && it.name == body.name },
            )
        }
    }

    @Test
    fun `should return 404 when board not found`() {
        val id = "missing-${java.util.UUID.randomUUID()}"
        val body = RequestGenerator.updateBoardBody()

        coEvery {
            updateBoardOperation.execute(any())
        } returns UpdateBoardOperation.Result.NotFound

        webClient
            .put()
            .uri("/api/v1/boards/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isNotFound
    }
}
