package com.kanban.http.task

import com.kanban.task.ArchiveTaskOperation
import io.mockk.coEvery
import io.mockk.coVerify
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера архивирования задачи.
 * Проверяют корректность кодов ответа.
 */
internal class ArchiveTaskControllerTest : BaseTaskControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(ArchiveTaskController::class.java)
    }

    @Test
    fun `should archive task and return 204`() {
        val id = "task-${UUID.randomUUID()}"

        coEvery {
            archiveTaskOperation.execute(any())
        } returns ArchiveTaskOperation.Result.Success

        webClient
            .post()
            .uri("/api/v1/tasks/$id/archive")
            .exchange()
            .expectStatus()
            .isNoContent

        coVerify {
            archiveTaskOperation.execute(match { it.taskId == id })
        }
    }

    @Test
    fun `should return 404 when task not found`() {
        val id = "missing-${UUID.randomUUID()}"

        coEvery {
            archiveTaskOperation.execute(any())
        } returns ArchiveTaskOperation.Result.NotFound

        webClient
            .post()
            .uri("/api/v1/tasks/$id/archive")
            .exchange()
            .expectStatus()
            .isNotFound
    }
}
