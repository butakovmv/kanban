package com.kanban.http.access

import com.kanban.access.CreatePermissionOperation
import com.kanban.access.Permission
import com.kanban.common.PermissionId
import io.mockk.coEvery
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

internal class CreatePermissionControllerTest : BaseAccessControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(CreatePermissionController::class.java)
    }

    @Test
    fun `should create permission and return 201`() {
        val request = AccessRequestGenerator.createPermissionBody()
        val permission =
            Permission(
                id = PermissionId("new-perm-id"),
                resource = request.resource,
                action = request.action,
                targetId = request.targetId,
                createdAt = Instant.now(),
            )

        coEvery {
            createPermissionOperation.execute(any())
        } returns CreatePermissionOperation.Result.Success(permission = permission)

        webClient
            .post()
            .uri("/api/v1/permissions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isCreated
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo("new-perm-id")
            .jsonPath("$.resource")
            .isEqualTo("project")
            .jsonPath("$.action")
            .isEqualTo("read")
    }

    @Test
    fun `should create permission with target and return 201`() {
        val request = AccessRequestGenerator.createPermissionBodyWithTarget()
        val permission =
            Permission(
                id = PermissionId("new-perm-id"),
                resource = request.resource,
                action = request.action,
                targetId = request.targetId,
                createdAt = Instant.now(),
            )

        coEvery {
            createPermissionOperation.execute(any())
        } returns CreatePermissionOperation.Result.Success(permission = permission)

        webClient
            .post()
            .uri("/api/v1/permissions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isCreated
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo("new-perm-id")
            .jsonPath("$.target_id")
            .isEqualTo(request.targetId!!)
    }

    @Test
    fun `should return 400 on failure`() {
        val request = AccessRequestGenerator.createPermissionBody()

        coEvery {
            createPermissionOperation.execute(any())
        } returns CreatePermissionOperation.Result.Failure("Invalid resource type")

        webClient
            .post()
            .uri("/api/v1/permissions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.reason")
            .isEqualTo("Invalid resource type")
    }
}
