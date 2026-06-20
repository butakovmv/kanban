package com.kanban.http.task

import com.kanban.common.BoardId
import com.kanban.common.ColumnId
import com.kanban.common.TaskId
import com.kanban.task.ListBoardBacklogOperation
import com.kanban.task.Task
import io.mockk.coEvery
import io.mockk.coVerify
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

internal class ListBoardBacklogControllerTest : BaseTaskControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(ListBoardBacklogController::class.java)
    }

    @Test
    fun `should return 200 with backlog tasks`() {
        val boardId = "board-${UUID.randomUUID()}"
        val tasks =
            listOf(
                Task(
                    id = TaskId("task-1"),
                    boardId = BoardId(boardId),
                    columnId = ColumnId("column-1"),
                    title = "Task 1",
                    description = null,
                    assigneeId = null,
                    position = 0,
                    dueDate = null,
                    archived = false,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                ),
            )

        coEvery {
            listBoardBacklogOperation.execute(any())
        } returns ListBoardBacklogOperation.Result.Success(tasks = tasks)

        webClient
            .get()
            .uri { builder -> builder.path("/api/v1/boards/$boardId/backlog").build() }
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.tasks.length()")
            .isEqualTo(1)
            .jsonPath("$.tasks[0].id")
            .isEqualTo("task-1")
            .jsonPath("$.tasks[0].board_id")
            .isEqualTo(boardId)

        coVerify {
            listBoardBacklogOperation.execute(match { it.boardId == boardId })
        }
    }

    @Test
    fun `should return 200 with empty list when no backlog tasks`() {
        val boardId = "board-${UUID.randomUUID()}"

        coEvery {
            listBoardBacklogOperation.execute(any())
        } returns ListBoardBacklogOperation.Result.Success(tasks = emptyList())

        webClient
            .get()
            .uri { builder -> builder.path("/api/v1/boards/$boardId/backlog").build() }
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.tasks.length()")
            .isEqualTo(0)
    }
}
