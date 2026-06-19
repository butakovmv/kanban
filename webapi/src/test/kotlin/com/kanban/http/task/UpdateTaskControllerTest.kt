package com.kanban.http.task

import com.kanban.common.BoardId
import com.kanban.common.ColumnId
import com.kanban.common.TaskId
import com.kanban.task.Task
import com.kanban.task.UpdateTaskOperation
import io.mockk.coEvery
import io.mockk.coVerify
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера обновления задачи.
 * Проверяют корректность кодов ответа и тел запросов/ответов.
 */
internal class UpdateTaskControllerTest : BaseTaskControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(UpdateTaskController::class.java)
    }

    @Test
    fun `should update task and return 200`() {
        val id = "task-${UUID.randomUUID()}"
        val body = RequestGenerator.updateTaskBody()
        val task =
            Task(
                id = TaskId(id),
                boardId = BoardId("board-1"),
                columnId = ColumnId("column-1"),
                title = body.title!!,
                description = body.description,
                assigneeId = body.assigneeId,
                position = 0,
                dueDate = body.dueDate,
                archived = false,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )

        coEvery {
            updateTaskOperation.execute(any())
        } returns UpdateTaskOperation.Result.Success(task = task)

        webClient
            .put()
            .uri("/api/v1/tasks/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo(id)
            .jsonPath("$.title")
            .isEqualTo(body.title)
            .jsonPath("$.description")
            .isEqualTo(body.description!!)

        coVerify {
            updateTaskOperation.execute(
                match {
                    it.taskId == id &&
                        it.title == body.title &&
                        it.description == body.description &&
                        it.assigneeId == body.assigneeId
                },
            )
        }
    }

    @Test
    fun `should return 404 when task not found`() {
        val id = "missing-${UUID.randomUUID()}"
        val body = RequestGenerator.updateTaskBody()

        coEvery {
            updateTaskOperation.execute(any())
        } returns UpdateTaskOperation.Result.NotFound

        webClient
            .put()
            .uri("/api/v1/tasks/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `should return 400 on failure`() {
        val id = "task-${UUID.randomUUID()}"
        val body = RequestGenerator.updateTaskBody()

        coEvery {
            updateTaskOperation.execute(any())
        } returns UpdateTaskOperation.Result.Failure("Title cannot be empty")

        webClient
            .put()
            .uri("/api/v1/tasks/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.reason")
            .isEqualTo("Title cannot be empty")
    }
}
