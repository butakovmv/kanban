package com.kanban.http.task

import com.kanban.common.BoardId
import com.kanban.common.ColumnId
import com.kanban.common.TaskId
import com.kanban.task.GetTaskOperation
import com.kanban.task.Task
import io.mockk.coEvery
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера получения задачи.
 * Проверяют корректность кодов ответа и тел запросов/ответов.
 */
internal class GetTaskControllerTest : BaseTaskControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(GetTaskController::class.java)
    }

    @Test
    fun `should return 200 with task when found`() {
        val id = "task-${UUID.randomUUID()}"
        val task =
            Task(
                id = TaskId(id),
                boardId = BoardId("board-1"),
                columnId = ColumnId("column-1"),
                title = "Test Task",
                description = "Test Description",
                assigneeId = "user-1",
                position = 0,
                dueDate = null,
                archived = false,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )

        coEvery {
            getTaskOperation.execute(any())
        } returns GetTaskOperation.Result.Success(task = task)

        webClient
            .get()
            .uri("/api/v1/tasks/$id")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo(id)
            .jsonPath("$.board_id")
            .isEqualTo("board-1")
            .jsonPath("$.column_id")
            .isEqualTo("column-1")
            .jsonPath("$.title")
            .isEqualTo("Test Task")
            .jsonPath("$.description")
            .isEqualTo("Test Description")
    }

    @Test
    fun `should return 404 when task not found`() {
        val id = "missing-${UUID.randomUUID()}"

        coEvery {
            getTaskOperation.execute(any())
        } returns GetTaskOperation.Result.NotFound

        webClient
            .get()
            .uri("/api/v1/tasks/$id")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound
    }
}
