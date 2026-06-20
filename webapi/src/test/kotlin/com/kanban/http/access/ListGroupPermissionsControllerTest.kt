package com.kanban.http.access

import com.kanban.access.ListGroupPermissionsOperation
import com.kanban.access.Permission
import com.kanban.common.PermissionId
import io.mockk.coEvery
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

internal class ListGroupPermissionsControllerTest : BaseAccessControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(ListGroupPermissionsController::class.java)
    }

    @Test
    fun `should return 200 with permissions list`() {
        val groupId = "group-${UUID.randomUUID()}"
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
            listGroupPermissionsOperation.execute(any())
        } returns ListGroupPermissionsOperation.Result.Success(permissions = permissions)

        webClient
            .get()
            .uri("/api/v1/groups/$groupId/permissions")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.permissions.length()")
            .isEqualTo(1)
            .jsonPath("$.permissions[0].id")
            .isEqualTo("p-1")
    }

    @Test
    fun `should return 200 with empty list when no permissions`() {
        val groupId = "group-${UUID.randomUUID()}"

        coEvery {
            listGroupPermissionsOperation.execute(any())
        } returns ListGroupPermissionsOperation.Result.Success(permissions = emptyList())

        webClient
            .get()
            .uri("/api/v1/groups/$groupId/permissions")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.permissions.length()")
            .isEqualTo(0)
    }
}
