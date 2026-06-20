package com.kanban.http.access

import com.kanban.access.RevokePermissionOperation
import io.mockk.coEvery
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient

internal class RevokePermissionControllerTest : BaseAccessControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(RevokePermissionController::class.java)
    }

    @Test
    fun `should revoke permission and return 204`() {
        val groupId = "group-${UUID.randomUUID()}"
        val permId = "perm-${UUID.randomUUID()}"

        coEvery {
            revokePermissionOperation.execute(any())
        } returns RevokePermissionOperation.Result.Success

        webClient
            .delete()
            .uri("/api/v1/groups/$groupId/permissions/$permId")
            .exchange()
            .expectStatus()
            .isNoContent
    }

    @Test
    fun `should return 400 on failure`() {
        val groupId = "group-${UUID.randomUUID()}"
        val permId = "perm-${UUID.randomUUID()}"

        coEvery {
            revokePermissionOperation.execute(any())
        } returns RevokePermissionOperation.Result.Failure("Permission not found")

        webClient
            .delete()
            .uri("/api/v1/groups/$groupId/permissions/$permId")
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.reason")
            .isEqualTo("Permission not found")
    }
}
