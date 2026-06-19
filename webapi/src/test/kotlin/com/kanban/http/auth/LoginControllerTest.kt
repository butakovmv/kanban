package com.kanban.http.auth

import com.kanban.common.AccessToken
import com.kanban.common.AuthTokens
import com.kanban.common.Email
import com.kanban.common.PasswordHash
import com.kanban.common.RefreshToken
import com.kanban.common.UserId
import com.kanban.identity.LoginWithPasswordOperation
import com.kanban.identity.User
import io.mockk.coEvery
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера входа по паролю.
 * Проверяют корректность кодов ответа и тел запросов/ответов.
 */
internal class LoginControllerTest : BaseControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(LoginController::class.java)
    }

    @Test
    fun `should login and return 200`() {
        val request = RequestGenerator.loginRequest()
        val user =
            User(
                id = UserId("user-1"),
                email = Email(request.email),
                passwordHash = PasswordHash("hashed"),
                displayName = "Test User",
                totpSecret = null,
                totpEnabled = false,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )
        val tokens = AuthTokens(AccessToken("access-token"), RefreshToken("refresh-token"))

        coEvery {
            loginWithPasswordOperation.execute(any())
        } returns LoginWithPasswordOperation.Result.Success(tokens = tokens, user = user)

        webClient
            .post()
            .uri("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.access_token")
            .isEqualTo("access-token")
            .jsonPath("$.refresh_token")
            .isEqualTo("refresh-token")
            .jsonPath("$.user.id")
            .isEqualTo("user-1")
    }

    @Test
    fun `should return 401 on wrong password`() {
        val request = RequestGenerator.loginRequest()

        coEvery {
            loginWithPasswordOperation.execute(any())
        } returns LoginWithPasswordOperation.Result.Failure("Invalid email or password")

        webClient
            .post()
            .uri("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isUnauthorized
            .expectBody()
            .jsonPath("$.reason")
            .isEqualTo("Invalid email or password")
    }
}
