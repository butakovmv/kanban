package com.kanban.http.project

import com.kanban.common.BoardId
import com.kanban.common.ColumnId
import com.kanban.project.Column
import com.kanban.project.ReorderColumnsOperation
import io.mockk.coEvery
import io.mockk.coVerify
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера реордеринга колонок.
 * Проверяют корректность кодов ответа и тел запросов/ответов.
 */
internal class ReorderColumnsControllerTest : BaseProjectControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(ReorderColumnsController::class.java)
    }

    @Test
    fun `should reorder columns and return 200 with new order`() {
        val boardId = "board-${java.util.UUID.randomUUID()}"
        val body = RequestGenerator.reorderColumnsBody()
        val columns =
            body.columnIds.mapIndexed { index, id ->
                Column(
                    id = ColumnId(id),
                    boardId = BoardId(boardId),
                    name = "Column $index",
                    position = index,
                    wipLimit = null,
                    createdAt = Instant.now(),
                )
            }

        coEvery {
            reorderColumnsOperation.execute(any())
        } returns ReorderColumnsOperation.Result.Success(columns = columns)

        webClient
            .put()
            .uri("/api/v1/boards/$boardId/columns/order")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.columns.length()")
            .isEqualTo(3)
            .jsonPath("$.columns[0].id")
            .isEqualTo(body.columnIds[0])
            .jsonPath("$.columns[1].id")
            .isEqualTo(body.columnIds[1])
            .jsonPath("$.columns[2].id")
            .isEqualTo(body.columnIds[2])

        coVerify {
            reorderColumnsOperation.execute(
                match { it.boardId == boardId && it.columnIds == body.columnIds },
            )
        }
    }

    @Test
    fun `should return 404 when board not found`() {
        val boardId = "missing-${java.util.UUID.randomUUID()}"
        val body = RequestGenerator.reorderColumnsBody()

        coEvery {
            reorderColumnsOperation.execute(any())
        } returns ReorderColumnsOperation.Result.BoardNotFound

        webClient
            .put()
            .uri("/api/v1/boards/$boardId/columns/order")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `should return 400 on invalid columns`() {
        val boardId = "board-${java.util.UUID.randomUUID()}"
        val body = RequestGenerator.reorderColumnsBody()

        coEvery {
            reorderColumnsOperation.execute(any())
        } returns ReorderColumnsOperation.Result.InvalidColumns

        webClient
            .put()
            .uri("/api/v1/boards/$boardId/columns/order")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.reason")
            .exists()
    }
}
