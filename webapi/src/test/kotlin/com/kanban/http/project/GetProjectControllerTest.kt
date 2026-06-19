package com.kanban.http.project

import com.kanban.common.ProjectId
import com.kanban.common.UserId
import com.kanban.project.GetProjectOperation
import com.kanban.project.Project
import io.mockk.coEvery
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера получения проекта.
 * Проверяют корректность кодов ответа и тел запросов/ответов.
 */
internal class GetProjectControllerTest : BaseProjectControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(GetProjectController::class.java)
    }

    @Test
    fun `should return 200 with project when found`() {
        val id = "project-${java.util.UUID.randomUUID()}"
        val project =
            Project(
                id = ProjectId(id),
                ownerId = UserId("owner-id"),
                name = "Test Project",
                description = "Test Description",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )

        coEvery {
            getProjectOperation.execute(any())
        } returns GetProjectOperation.Result.Success(project = project)

        webClient
            .get()
            .uri("/api/v1/projects/$id")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo(id)
            .jsonPath("$.name")
            .isEqualTo("Test Project")
            .jsonPath("$.description")
            .isEqualTo("Test Description")
    }

    @Test
    fun `should return 404 when project not found`() {
        val id = "missing-${java.util.UUID.randomUUID()}"

        coEvery {
            getProjectOperation.execute(any())
        } returns GetProjectOperation.Result.NotFound

        webClient
            .get()
            .uri("/api/v1/projects/$id")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound
    }
}
