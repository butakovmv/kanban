package com.kanban.http.access

import com.kanban.access.Group
import com.kanban.access.UpdateGroupOperation
import com.kanban.common.GroupId
import io.mockk.coEvery
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

internal class UpdateGroupControllerTest : BaseAccessControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(UpdateGroupController::class.java)
    }

    @Test
    fun `should update group and return 200`() {
        val id = "group-${UUID.randomUUID()}"
        val body = AccessRequestGenerator.updateGroupBody()
        val group =
            Group(
                id = GroupId(id),
                name = body.name!!,
                description = body.description,
                createdAt = Instant.now(),
            )

        coEvery {
            updateGroupOperation.execute(any())
        } returns UpdateGroupOperation.Result.Success(group = group)

        webClient
            .put()
            .uri("/api/v1/groups/$id")
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
    }

    @Test
    fun `should return 404 when group not found`() {
        val id = "missing-${UUID.randomUUID()}"
        val body = AccessRequestGenerator.updateGroupBody()

        coEvery {
            updateGroupOperation.execute(any())
        } returns UpdateGroupOperation.Result.NotFound

        webClient
            .put()
            .uri("/api/v1/groups/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `should return 400 on validation failure`() {
        val id = "group-${UUID.randomUUID()}"
        val body = AccessRequestGenerator.updateGroupBody()

        coEvery {
            updateGroupOperation.execute(any())
        } returns UpdateGroupOperation.Result.Failure("Invalid group name")

        webClient
            .put()
            .uri("/api/v1/groups/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.reason")
            .isEqualTo("Invalid group name")
    }
}
