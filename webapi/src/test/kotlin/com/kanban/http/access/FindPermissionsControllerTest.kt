package com.kanban.http.access

import com.kanban.access.FindPermissionsOperation
import com.kanban.access.Permission
import com.kanban.common.PermissionId
import io.mockk.coEvery
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

internal class FindPermissionsControllerTest : BaseAccessControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(FindPermissionsController::class.java)
    }

    @Test
    fun `should return 200 with permissions list`() {
        val permissions =
            listOf(
                Permission(
                    id = PermissionId("p-1"),
                    resource = "project",
                    action = "read",
                    targetId = null,
                    createdAt = Instant.now(),
                ),
            )

        coEvery {
            findPermissionsOperation.execute(any())
        } returns FindPermissionsOperation.Result.Success(permissions = permissions)

        webClient
            .get()
            .uri("/api/v1/permissions?resource=project&target_id=")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.permissions.length()")
            .isEqualTo(1)
            .jsonPath("$.permissions[0].id")
            .isEqualTo("p-1")
            .jsonPath("$.permissions[0].resource")
            .isEqualTo("project")
    }

    @Test
    fun `should return 200 with empty list when no permissions`() {
        coEvery {
            findPermissionsOperation.execute(any())
        } returns FindPermissionsOperation.Result.Success(permissions = emptyList())

        webClient
            .get()
            .uri("/api/v1/permissions?resource=project&target_id=")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.permissions.length()")
            .isEqualTo(0)
    }
}
