package com.kanban.http.task

import com.kanban.common.BoardId
import com.kanban.common.ColumnId
import com.kanban.common.TaskId
import com.kanban.task.CreateTaskOperation
import com.kanban.task.Task
import io.mockk.coEvery
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера создания задачи.
 * Проверяют корректность кодов ответа и тел запросов/ответов.
 */
internal class CreateTaskControllerTest : BaseTaskControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(CreateTaskController::class.java)
    }

    @Test
    fun `should create task and return 201`() {
        val request = RequestGenerator.createTaskRequest()
        val task =
            Task(
                id = TaskId("new-task-id"),
                boardId = BoardId(request.boardId),
                columnId = ColumnId(request.columnId),
                title = request.title,
                description = request.description,
                assigneeId = request.assigneeId,
                position = 0,
                dueDate = request.dueDate,
                archived = false,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )

        coEvery {
            createTaskOperation.execute(any())
        } returns CreateTaskOperation.Result.Success(task = task)

        webClient
            .post()
            .uri("/api/v1/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isCreated
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo("new-task-id")
            .jsonPath("$.board_id")
            .isEqualTo(request.boardId)
            .jsonPath("$.column_id")
            .isEqualTo(request.columnId)
            .jsonPath("$.title")
            .isEqualTo(request.title)
            .jsonPath("$.description")
            .isEqualTo(request.description!!)
            .jsonPath("$.assignee_id")
            .isEqualTo(request.assigneeId!!)
            .jsonPath("$.archived")
            .isEqualTo(false)
    }

    @Test
    fun `should create task without optional fields and return 201`() {
        val request = RequestGenerator.createTaskRequestWithoutOptionals()
        val task =
            Task(
                id = TaskId("new-task-id"),
                boardId = BoardId(request.boardId),
                columnId = ColumnId(request.columnId),
                title = request.title,
                description = null,
                assigneeId = null,
                position = 0,
                dueDate = null,
                archived = false,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )

        coEvery {
            createTaskOperation.execute(any())
        } returns CreateTaskOperation.Result.Success(task = task)

        webClient
            .post()
            .uri("/api/v1/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isCreated
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo("new-task-id")
            .jsonPath("$.description")
            .doesNotExist()
            .jsonPath("$.assignee_id")
            .doesNotExist()
            .jsonPath("$.due_date")
            .doesNotExist()
    }

    @Test
    fun `should return 400 on failure`() {
        val request = RequestGenerator.createTaskRequest()

        coEvery {
            createTaskOperation.execute(any())
        } returns CreateTaskOperation.Result.Failure("Board not found")

        webClient
            .post()
            .uri("/api/v1/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.reason")
            .isEqualTo("Board not found")
    }
}
