package com.kanban.http.project

import com.kanban.common.BoardId
import com.kanban.common.ColumnId
import com.kanban.common.ProjectId
import com.kanban.project.Board
import com.kanban.project.BoardView
import com.kanban.project.Column
import com.kanban.project.CreateBoardOperation
import io.mockk.coEvery
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера создания доски.
 * Проверяют корректность кодов ответа и тел запросов/ответов.
 */
internal class CreateBoardControllerTest : BaseProjectControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(CreateBoardController::class.java)
    }

    @Test
    fun `should create board and return 201`() {
        val request = RequestGenerator.createBoardRequest()
        val boardId = "board-${java.util.UUID.randomUUID()}"
        val board =
            Board(
                id = BoardId(boardId),
                projectId = ProjectId(request.projectId),
                name = request.name,
                position = 0,
                createdAt = Instant.now(),
            )
        val columns =
            listOf(
                Column(
                    id = ColumnId("col-1"),
                    boardId = BoardId(boardId),
                    name = "Todo",
                    position = 0,
                    wipLimit = null,
                    createdAt = Instant.now(),
                ),
            )
        val view = BoardView(board = board, columns = columns)

        coEvery {
            createBoardOperation.execute(any())
        } returns CreateBoardOperation.Result.Success(view = view)

        webClient
            .post()
            .uri("/api/v1/boards")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isCreated
            .expectBody()
            .jsonPath("$.board.id")
            .isEqualTo(boardId)
            .jsonPath("$.board.project_id")
            .isEqualTo(request.projectId)
            .jsonPath("$.board.name")
            .isEqualTo(request.name)
            .jsonPath("$.columns.length()")
            .isEqualTo(1)
    }

    @Test
    fun `should return 400 on failure`() {
        val request = RequestGenerator.createBoardRequest()

        coEvery {
            createBoardOperation.execute(any())
        } returns CreateBoardOperation.Result.Failure("Project not found")

        webClient
            .post()
            .uri("/api/v1/boards")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.reason")
            .isEqualTo("Project not found")
    }
}
