package com.kanban.http.access

import com.kanban.access.CreateGroupOperation
import com.kanban.access.Group
import com.kanban.common.GroupId
import io.mockk.coEvery
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

internal class CreateGroupControllerTest : BaseAccessControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(CreateGroupController::class.java)
    }

    @Test
    fun `should create group and return 201`() {
        val request = AccessRequestGenerator.createGroupBody()
        val group =
            Group(
                id = GroupId("new-group-id"),
                name = request.name,
                description = request.description,
                createdAt = Instant.now(),
            )

        coEvery {
            createGroupOperation.execute(any())
        } returns CreateGroupOperation.Result.Success(group = group)

        webClient
            .post()
            .uri("/api/v1/groups")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isCreated
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo("new-group-id")
            .jsonPath("$.name")
            .isEqualTo(request.name)
            .jsonPath("$.description")
            .isEqualTo(request.description!!)
    }

    @Test
    fun `should create group without description and return 201`() {
        val request = AccessRequestGenerator.createGroupBodyWithoutDescription()
        val group =
            Group(
                id = GroupId("new-group-id"),
                name = request.name,
                description = null,
                createdAt = Instant.now(),
            )

        coEvery {
            createGroupOperation.execute(any())
        } returns CreateGroupOperation.Result.Success(group = group)

        webClient
            .post()
            .uri("/api/v1/groups")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isCreated
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo("new-group-id")
            .jsonPath("$.description")
            .doesNotExist()
    }

    @Test
    fun `should return 400 on failure`() {
        val request = AccessRequestGenerator.createGroupBody()

        coEvery {
            createGroupOperation.execute(any())
        } returns CreateGroupOperation.Result.Failure("Group name is required")

        webClient
            .post()
            .uri("/api/v1/groups")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.reason")
            .isEqualTo("Group name is required")
    }
}
