package com.kanban.http.task

import com.kanban.task.DeleteTaskOperation
import io.mockk.coEvery
import io.mockk.coVerify
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера удаления задачи.
 * Проверяют корректность кодов ответа.
 */
internal class DeleteTaskControllerTest : BaseTaskControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(DeleteTaskController::class.java)
    }

    @Test
    fun `should delete task and return 204`() {
        val id = "task-${UUID.randomUUID()}"

        coEvery {
            deleteTaskOperation.execute(any())
        } returns DeleteTaskOperation.Result.Success

        webClient
            .delete()
            .uri("/api/v1/tasks/$id")
            .exchange()
            .expectStatus()
            .isNoContent

        coVerify {
            deleteTaskOperation.execute(match { it.taskId == id })
        }
    }

    @Test
    fun `should return 404 when task not found`() {
        val id = "missing-${UUID.randomUUID()}"

        coEvery {
            deleteTaskOperation.execute(any())
        } returns DeleteTaskOperation.Result.NotFound

        webClient
            .delete()
            .uri("/api/v1/tasks/$id")
            .exchange()
            .expectStatus()
            .isNotFound
    }
}
