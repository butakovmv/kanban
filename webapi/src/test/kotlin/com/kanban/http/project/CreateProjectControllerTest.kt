package com.kanban.http.project

import com.kanban.common.ProjectId
import com.kanban.common.UserId
import com.kanban.project.CreateProjectOperation
import com.kanban.project.Project
import io.mockk.coEvery
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера создания проекта.
 * Проверяют корректность кодов ответа и тел запросов/ответов.
 */
internal class CreateProjectControllerTest : BaseProjectControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(CreateProjectController::class.java)
    }

    @Test
    fun `should create project and return 201`() {
        val request = RequestGenerator.createProjectRequest()
        val project =
            Project(
                id = ProjectId("new-project-id"),
                ownerId = UserId(request.ownerId),
                name = request.name,
                description = request.description,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )

        coEvery {
            createProjectOperation.execute(any())
        } returns CreateProjectOperation.Result.Success(project = project)

        webClient
            .post()
            .uri("/api/v1/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isCreated
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo("new-project-id")
            .jsonPath("$.owner_id")
            .isEqualTo(request.ownerId)
            .jsonPath("$.name")
            .isEqualTo(request.name)
            .jsonPath("$.description")
            .isEqualTo(request.description!!)
    }

    @Test
    fun `should create project without description and return 201`() {
        val request = RequestGenerator.createProjectRequestWithoutDescription()
        val project =
            Project(
                id = ProjectId("new-project-id"),
                ownerId = UserId(request.ownerId),
                name = request.name,
                description = null,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )

        coEvery {
            createProjectOperation.execute(any())
        } returns CreateProjectOperation.Result.Success(project = project)

        webClient
            .post()
            .uri("/api/v1/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isCreated
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo("new-project-id")
            .jsonPath("$.description")
            .doesNotExist()
    }

    @Test
    fun `should return 400 on failure`() {
        val request = RequestGenerator.createProjectRequest()

        coEvery {
            createProjectOperation.execute(any())
        } returns CreateProjectOperation.Result.Failure("Project limit exceeded")

        webClient
            .post()
            .uri("/api/v1/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.reason")
            .isEqualTo("Project limit exceeded")
    }
}
