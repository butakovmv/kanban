package com.kanban.http.project

import com.kanban.common.BoardId
import com.kanban.common.ColumnId
import com.kanban.common.ProjectId
import com.kanban.project.Board
import com.kanban.project.BoardView
import com.kanban.project.Column
import com.kanban.project.GetBoardOperation
import io.mockk.coEvery
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера получения доски.
 * Проверяют корректность кодов ответа и тел запросов/ответов.
 */
internal class GetBoardControllerTest : BaseProjectControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(GetBoardController::class.java)
    }

    @Test
    fun `should return 200 with board and columns when found`() {
        val boardId = "board-${java.util.UUID.randomUUID()}"
        val projectId = "project-${java.util.UUID.randomUUID()}"
        val board =
            Board(
                id = BoardId(boardId),
                projectId = ProjectId(projectId),
                name = "Test Board",
                position = 1,
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
            getBoardOperation.execute(any())
        } returns GetBoardOperation.Result.Success(view = view)

        webClient
            .get()
            .uri("/api/v1/boards/$boardId")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.board.id")
            .isEqualTo(boardId)
            .jsonPath("$.board.project_id")
            .isEqualTo(projectId)
            .jsonPath("$.board.name")
            .isEqualTo("Test Board")
            .jsonPath("$.columns.length()")
            .isEqualTo(1)
    }

    @Test
    fun `should include wip_limit in column response when set`() {
        val boardId = "board-${java.util.UUID.randomUUID()}"
        val board =
            Board(
                id = BoardId(boardId),
                projectId = ProjectId("project-1"),
                name = "Test Board",
                position = 1,
                createdAt = Instant.now(),
            )
        val columns =
            listOf(
                Column(
                    id = ColumnId("col-1"),
                    boardId = BoardId(boardId),
                    name = "In Progress",
                    position = 0,
                    wipLimit = 5,
                    createdAt = Instant.now(),
                ),
            )
        val view = BoardView(board = board, columns = columns)

        coEvery {
            getBoardOperation.execute(any())
        } returns GetBoardOperation.Result.Success(view = view)

        webClient
            .get()
            .uri("/api/v1/boards/$boardId")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.columns[0].wip_limit")
            .isEqualTo(5)
    }

    @Test
    fun `should return 404 when board not found`() {
        val boardId = "missing-${java.util.UUID.randomUUID()}"

        coEvery {
            getBoardOperation.execute(any())
        } returns GetBoardOperation.Result.NotFound

        webClient
            .get()
            .uri("/api/v1/boards/$boardId")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound
    }
}
