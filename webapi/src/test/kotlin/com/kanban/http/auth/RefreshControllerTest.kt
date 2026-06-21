package com.kanban.http.auth

import com.kanban.common.AccessToken
import com.kanban.common.AuthTokens
import com.kanban.common.RefreshToken
import com.kanban.identity.RefreshTokenOperation
import io.mockk.coEvery
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

internal class RefreshControllerTest : BaseControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(RefreshController::class.java)
    }

    @Test
    fun `should refresh tokens and return 200`() {
        val body = RequestGenerator.refreshBody()
        val newTokens = AuthTokens(AccessToken("new-access"), RefreshToken("new-refresh"))

        coEvery {
            refreshTokenOperation.execute(any())
        } returns RefreshTokenOperation.Result.Success(tokens = newTokens)

        webClient
            .post()
            .uri("/api/v1/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.access_token")
            .isEqualTo("new-access")
            .jsonPath("$.refresh_token")
            .isEqualTo("new-refresh")
    }

    @Test
    fun `should return 401 on invalid refresh token`() {
        val body = RequestGenerator.refreshBody()

        coEvery {
            refreshTokenOperation.execute(any())
        } returns RefreshTokenOperation.Result.Failure("Invalid refresh token")

        webClient
            .post()
            .uri("/api/v1/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isUnauthorized
            .expectBody()
            .jsonPath("$.reason")
            .isEqualTo("Invalid refresh token")
    }
}
