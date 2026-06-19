package com.kanban.http.project

import com.kanban.common.ProjectId
import com.kanban.common.UserId
import com.kanban.project.ListProjectsOperation
import com.kanban.project.Project
import io.mockk.coEvery
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера получения списка проектов.
 * Проверяют корректность кодов ответа и тел запросов/ответов.
 */
internal class ListProjectsControllerTest : BaseProjectControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(ListProjectsController::class.java)
    }

    @Test
    fun `should return 200 with projects list`() {
        val ownerId = "owner-${java.util.UUID.randomUUID()}"
        val projects =
            listOf(
                Project(
                    id = ProjectId("p-1"),
                    ownerId = UserId(ownerId),
                    name = "Project 1",
                    description = "Desc 1",
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                ),
                Project(
                    id = ProjectId("p-2"),
                    ownerId = UserId(ownerId),
                    name = "Project 2",
                    description = null,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                ),
            )

        coEvery {
            listProjectsOperation.execute(any())
        } returns ListProjectsOperation.Result.Success(projects = projects)

        webClient
            .get()
            .uri { builder -> builder.path("/api/v1/projects").queryParam("owner_id", ownerId).build() }
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.projects.length()")
            .isEqualTo(2)
            .jsonPath("$.projects[0].id")
            .isEqualTo("p-1")
            .jsonPath("$.projects[0].owner_id")
            .isEqualTo(ownerId)
            .jsonPath("$.projects[1].id")
            .isEqualTo("p-2")
    }

    @Test
    fun `should return 200 with empty list when no projects`() {
        val ownerId = "owner-${java.util.UUID.randomUUID()}"

        coEvery {
            listProjectsOperation.execute(any())
        } returns ListProjectsOperation.Result.Success(projects = emptyList())

        webClient
            .get()
            .uri { builder -> builder.path("/api/v1/projects").queryParam("owner_id", ownerId).build() }
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.projects.length()")
            .isEqualTo(0)
    }
}
