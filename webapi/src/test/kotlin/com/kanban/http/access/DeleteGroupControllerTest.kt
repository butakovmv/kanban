package com.kanban.http.access

import com.kanban.access.DeleteGroupOperation
import io.mockk.coEvery
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient

internal class DeleteGroupControllerTest : BaseAccessControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(DeleteGroupController::class.java)
    }

    @Test
    fun `should delete group and return 204`() {
        val id = "group-${UUID.randomUUID()}"

        coEvery {
            deleteGroupOperation.execute(any())
        } returns DeleteGroupOperation.Result.Success

        webClient
            .delete()
            .uri("/api/v1/groups/$id")
            .exchange()
            .expectStatus()
            .isNoContent
    }

    @Test
    fun `should return 404 when group not found`() {
        val id = "missing-${UUID.randomUUID()}"

        coEvery {
            deleteGroupOperation.execute(any())
        } returns DeleteGroupOperation.Result.NotFound

        webClient
            .delete()
            .uri("/api/v1/groups/$id")
            .exchange()
            .expectStatus()
            .isNotFound
    }
}
