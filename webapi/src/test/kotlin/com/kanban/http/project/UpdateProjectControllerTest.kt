package com.kanban.http.project

import com.kanban.common.ProjectId
import com.kanban.common.UserId
import com.kanban.project.Project
import com.kanban.project.UpdateProjectOperation
import io.mockk.coEvery
import io.mockk.coVerify
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера обновления проекта.
 * Проверяют корректность кодов ответа и тел запросов/ответов.
 */
internal class UpdateProjectControllerTest : BaseProjectControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(UpdateProjectController::class.java)
    }

    @Test
    fun `should update project and return 200`() {
        val id = "project-${java.util.UUID.randomUUID()}"
        val body = RequestGenerator.updateProjectBody()
        val project =
            Project(
                id = ProjectId(id),
                ownerId = UserId("owner-id"),
                name = body.name!!,
                description = body.description,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )

        coEvery {
            updateProjectOperation.execute(any())
        } returns UpdateProjectOperation.Result.Success(project = project)

        webClient
            .put()
            .uri("/api/v1/projects/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo(id)
            .jsonPath("$.name")
            .isEqualTo(body.name)
            .jsonPath("$.description")
            .isEqualTo(body.description!!)

        coVerify {
            updateProjectOperation.execute(
                match { it.projectId == id && it.name == body.name && it.description == body.description },
            )
        }
    }

    @Test
    fun `should return 404 when project not found`() {
        val id = "missing-${java.util.UUID.randomUUID()}"
        val body = RequestGenerator.updateProjectBody()

        coEvery {
            updateProjectOperation.execute(any())
        } returns UpdateProjectOperation.Result.NotFound

        webClient
            .put()
            .uri("/api/v1/projects/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isNotFound
    }
}
