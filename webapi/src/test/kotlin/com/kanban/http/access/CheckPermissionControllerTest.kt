package com.kanban.http.access

import com.kanban.access.CheckPermissionOperation
import io.mockk.coEvery
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

internal class CheckPermissionControllerTest : BaseAccessControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(CheckPermissionController::class.java)
    }

    @Test
    fun `should return 200 with allowed true when permission granted`() {
        val userId = "user-${UUID.randomUUID()}"

        coEvery {
            checkPermissionOperation.execute(any())
        } returns CheckPermissionOperation.Result.Allowed

        webClient
            .get()
            .uri("/api/v1/permissions/check?user_id=$userId&resource=project&action=read&target_id=")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.allowed")
            .isEqualTo(true)
            .jsonPath("$.reason")
            .doesNotExist()
    }

    @Test
    fun `should return 200 with allowed false when permission denied`() {
        val userId = "user-${UUID.randomUUID()}"

        coEvery {
            checkPermissionOperation.execute(any())
        } returns CheckPermissionOperation.Result.Denied("Insufficient permissions")

        webClient
            .get()
            .uri("/api/v1/permissions/check?user_id=$userId&resource=project&action=admin&target_id=target-123")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.allowed")
            .isEqualTo(false)
            .jsonPath("$.reason")
            .isEqualTo("Insufficient permissions")
    }
}
