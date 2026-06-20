package com.kanban.http.access

import com.kanban.access.RemoveMemberOperation
import io.mockk.coEvery
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient

internal class RemoveMemberControllerTest : BaseAccessControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(RemoveMemberController::class.java)
    }

    @Test
    fun `should remove member and return 204`() {
        val groupId = "group-${UUID.randomUUID()}"
        val userId = "user-${UUID.randomUUID()}"

        coEvery {
            removeMemberOperation.execute(any())
        } returns RemoveMemberOperation.Result.Success

        webClient
            .delete()
            .uri("/api/v1/groups/$groupId/members/$userId")
            .exchange()
            .expectStatus()
            .isNoContent
    }

    @Test
    fun `should return 400 on failure`() {
        val groupId = "group-${UUID.randomUUID()}"
        val userId = "user-${UUID.randomUUID()}"

        coEvery {
            removeMemberOperation.execute(any())
        } returns RemoveMemberOperation.Result.Failure("User is not a member")

        webClient
            .delete()
            .uri("/api/v1/groups/$groupId/members/$userId")
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.reason")
            .isEqualTo("User is not a member")
    }
}
