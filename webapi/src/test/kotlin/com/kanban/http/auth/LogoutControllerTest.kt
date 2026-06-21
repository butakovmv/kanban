package com.kanban.http.auth

import com.kanban.identity.LogoutOperation
import io.mockk.coEvery
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

internal class LogoutControllerTest : BaseControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(LogoutController::class.java)
    }

    @Test
    fun `should logout and return 204`() {
        val body = RequestGenerator.logoutBody()

        coEvery { logoutOperation.execute(any()) } returns LogoutOperation.Result.Success

        webClient
            .post()
            .uri("/api/v1/auth/logout")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isNoContent
    }
}
