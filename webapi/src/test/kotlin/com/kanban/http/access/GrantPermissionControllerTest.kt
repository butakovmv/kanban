package com.kanban.http.access

import com.kanban.access.GrantPermissionOperation
import io.mockk.coEvery
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

internal class GrantPermissionControllerTest : BaseAccessControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(GrantPermissionController::class.java)
    }

    @Test
    fun `should grant permission and return 201`() {
        val groupId = "group-${UUID.randomUUID()}"
        val body = AccessRequestGenerator.grantPermissionBody()

        coEvery {
            grantPermissionOperation.execute(any())
        } returns GrantPermissionOperation.Result.Success

        webClient
            .post()
            .uri("/api/v1/groups/$groupId/permissions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isCreated
    }

    @Test
    fun `should return 400 on failure`() {
        val groupId = "group-${UUID.randomUUID()}"
        val body = AccessRequestGenerator.grantPermissionBody()

        coEvery {
            grantPermissionOperation.execute(any())
        } returns GrantPermissionOperation.Result.Failure("Group not found")

        webClient
            .post()
            .uri("/api/v1/groups/$groupId/permissions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.reason")
            .isEqualTo("Group not found")
    }
}
