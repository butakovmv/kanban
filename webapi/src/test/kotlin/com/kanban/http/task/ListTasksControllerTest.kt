package com.kanban.http.task

import com.kanban.common.BoardId
import com.kanban.common.ColumnId
import com.kanban.common.TaskId
import com.kanban.task.ListTasksOperation
import com.kanban.task.Task
import io.mockk.coEvery
import io.mockk.coVerify
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера получения списка задач доски.
 * Проверяют корректность кодов ответа и тел запросов/ответов.
 */
internal class ListTasksControllerTest : BaseTaskControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(ListTasksController::class.java)
    }

    @Test
    fun `should return 200 with tasks list`() {
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
                Task(
                    id = TaskId("task-2"),
                    boardId = BoardId(boardId),
                    columnId = ColumnId("column-1"),
                    title = "Task 2",
                    description = "Desc 2",
                    assigneeId = "user-1",
                    position = 1,
                    dueDate = null,
                    archived = false,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                ),
            )

        coEvery {
            listTasksOperation.execute(any())
        } returns ListTasksOperation.Result.Success(tasks = tasks)

        webClient
            .get()
            .uri { builder -> builder.path("/api/v1/boards/$boardId/tasks").build() }
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.tasks.length()")
            .isEqualTo(2)
            .jsonPath("$.tasks[0].id")
            .isEqualTo("task-1")
            .jsonPath("$.tasks[0].board_id")
            .isEqualTo(boardId)
            .jsonPath("$.tasks[1].id")
            .isEqualTo("task-2")
            .jsonPath("$.tasks[1].title")
            .isEqualTo("Task 2")

        coVerify {
            listTasksOperation.execute(match { it.boardId == boardId && !it.includeArchived })
        }
    }

    @Test
    fun `should return 200 with empty list when no tasks`() {
        val boardId = "board-${UUID.randomUUID()}"

        coEvery {
            listTasksOperation.execute(any())
        } returns ListTasksOperation.Result.Success(tasks = emptyList())

        webClient
            .get()
            .uri { builder -> builder.path("/api/v1/boards/$boardId/tasks").build() }
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.tasks.length()")
            .isEqualTo(0)
    }

    @Test
    fun `should pass include_archived query param when true`() {
        val boardId = "board-${UUID.randomUUID()}"

        coEvery {
            listTasksOperation.execute(any())
        } returns ListTasksOperation.Result.Success(tasks = emptyList())

        webClient
            .get()
            .uri { builder ->
                builder
                    .path("/api/v1/boards/$boardId/tasks")
                    .queryParam("include_archived", "true")
                    .build()
            }.accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk

        coVerify {
            listTasksOperation.execute(match { it.boardId == boardId && it.includeArchived })
        }
    }
}
