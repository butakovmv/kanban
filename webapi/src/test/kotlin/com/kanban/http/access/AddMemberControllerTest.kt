package com.kanban.http.access

import com.kanban.access.AddMemberOperation
import io.mockk.coEvery
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

internal class AddMemberControllerTest : BaseAccessControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(AddMemberController::class.java)
    }

    @Test
    fun `should add member and return 201`() {
        val groupId = "group-${UUID.randomUUID()}"
        val body = AccessRequestGenerator.addMemberBody()

        coEvery {
            addMemberOperation.execute(any())
        } returns AddMemberOperation.Result.Success

        webClient
            .post()
            .uri("/api/v1/groups/$groupId/members")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isCreated
    }

    @Test
    fun `should return 400 on failure`() {
        val groupId = "group-${UUID.randomUUID()}"
        val body = AccessRequestGenerator.addMemberBody()

        coEvery {
            addMemberOperation.execute(any())
        } returns AddMemberOperation.Result.Failure("User is already a member")

        webClient
            .post()
            .uri("/api/v1/groups/$groupId/members")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.reason")
            .isEqualTo("User is already a member")
    }
}
