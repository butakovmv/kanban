package com.kanban.http.project

import com.kanban.project.DeleteProjectOperation
import io.mockk.coEvery
import io.mockk.coVerify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера удаления проекта.
 * Проверяют корректность кодов ответа.
 */
internal class DeleteProjectControllerTest : BaseProjectControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(DeleteProjectController::class.java)
    }

    @Test
    fun `should delete project and return 204`() {
        val id = "project-${java.util.UUID.randomUUID()}"

        coEvery {
            deleteProjectOperation.execute(any())
        } returns DeleteProjectOperation.Result.Success

        webClient
            .delete()
            .uri("/api/v1/projects/$id")
            .exchange()
            .expectStatus()
            .isNoContent

        coVerify {
            deleteProjectOperation.execute(match { it.projectId == id })
        }
    }

    @Test
    fun `should return 404 when project not found`() {
        val id = "missing-${java.util.UUID.randomUUID()}"

        coEvery {
            deleteProjectOperation.execute(any())
        } returns DeleteProjectOperation.Result.NotFound

        webClient
            .delete()
            .uri("/api/v1/projects/$id")
            .exchange()
            .expectStatus()
            .isNotFound
    }
}
