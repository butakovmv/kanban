package com.kanban.http.access

import com.kanban.access.DeletePermissionOperation
import io.mockk.coEvery
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient

internal class DeletePermissionControllerTest : BaseAccessControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(DeletePermissionController::class.java)
    }

    @Test
    fun `should delete permission and return 204`() {
        val id = "perm-${UUID.randomUUID()}"

        coEvery {
            deletePermissionOperation.execute(any())
        } returns DeletePermissionOperation.Result.Success

        webClient
            .delete()
            .uri("/api/v1/permissions/$id")
            .exchange()
            .expectStatus()
            .isNoContent
    }

    @Test
    fun `should return 404 when permission not found`() {
        val id = "missing-${UUID.randomUUID()}"

        coEvery {
            deletePermissionOperation.execute(any())
        } returns DeletePermissionOperation.Result.NotFound

        webClient
            .delete()
            .uri("/api/v1/permissions/$id")
            .exchange()
            .expectStatus()
            .isNotFound
    }
}
