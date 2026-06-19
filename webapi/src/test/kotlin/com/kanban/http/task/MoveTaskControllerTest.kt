package com.kanban.http.task

import com.kanban.common.BoardId
import com.kanban.common.ColumnId
import com.kanban.common.TaskId
import com.kanban.task.MoveTaskOperation
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
 * Тесты контроллера перемещения задачи.
 * Проверяют корректность кодов ответа и тел запросов/ответов.
 */
internal class MoveTaskControllerTest : BaseTaskControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(MoveTaskController::class.java)
    }

    @Test
    fun `should move task and return 200`() {
        val id = "task-${UUID.randomUUID()}"
        val body = RequestGenerator.moveTaskBody()
        val task =
            Task(
                id = TaskId(id),
                boardId = BoardId("board-1"),
                columnId = ColumnId(body.columnId),
                title = "Moved Task",
                description = null,
                assigneeId = null,
                position = body.position,
                dueDate = null,
                archived = false,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )

        coEvery {
            moveTaskOperation.execute(any())
        } returns MoveTaskOperation.Result.Success(task = task)

        webClient
            .patch()
            .uri("/api/v1/tasks/$id/move")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo(id)
            .jsonPath("$.column_id")
            .isEqualTo(body.columnId)
            .jsonPath("$.position")
            .isEqualTo(body.position)

        coVerify {
            moveTaskOperation.execute(
                match {
                    it.taskId == id &&
                        it.columnId == body.columnId &&
                        it.position == body.position
                },
            )
        }
    }

    @Test
    fun `should return 404 when task not found`() {
        val id = "missing-${UUID.randomUUID()}"
        val body = RequestGenerator.moveTaskBody()

        coEvery {
            moveTaskOperation.execute(any())
        } returns MoveTaskOperation.Result.NotFound

        webClient
            .patch()
            .uri("/api/v1/tasks/$id/move")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isNotFound
    }
}
