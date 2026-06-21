package com.kanban.http.auth

import com.kanban.common.AccessToken
import com.kanban.common.AuthTokens
import com.kanban.common.Email
import com.kanban.common.PasswordHash
import com.kanban.common.RefreshToken
import com.kanban.common.UserId
import com.kanban.identity.RegisterUserOperation
import com.kanban.identity.User
import io.mockk.coEvery
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

internal class RegisterControllerTest : BaseControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(RegisterController::class.java)
    }

    @Test
    fun `should register user and return 201`() {
        val body = RequestGenerator.registerBody()
        val user =
            User(
                id = UserId("new-user-id"),
                email = Email(body.email),
                passwordHash = PasswordHash("hashed"),
                displayName = body.displayName,
                totpSecret = null,
                totpEnabled = false,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )
        val tokens = AuthTokens(AccessToken("access-token"), RefreshToken("refresh-token"))

        coEvery {
            registerUserOperation.execute(any())
        } returns RegisterUserOperation.Result.Success(tokens = tokens, user = user)

        webClient
            .post()
            .uri("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isCreated
            .expectBody()
            .jsonPath("$.access_token")
            .isEqualTo("access-token")
            .jsonPath("$.refresh_token")
            .isEqualTo("refresh-token")
            .jsonPath("$.user.id")
            .isEqualTo("new-user-id")
            .jsonPath("$.user.email")
            .isEqualTo(body.email)
            .jsonPath("$.user.display_name")
            .isEqualTo(body.displayName)
    }

    @Test
    fun `should return 400 on duplicate email`() {
        val body = RequestGenerator.registerBody()

        coEvery {
            registerUserOperation.execute(any())
        } returns RegisterUserOperation.Result.Failure("Email already registered")

        webClient
            .post()
            .uri("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.reason")
            .isEqualTo("Email already registered")
    }

    @Test
    fun `should return 400 on invalid email`() {
        val body = RequestGenerator.registerBody()

        coEvery {
            registerUserOperation.execute(any())
        } returns RegisterUserOperation.Result.Failure("Invalid email: ${body.email}")

        webClient
            .post()
            .uri("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.reason")
            .isEqualTo("Invalid email: ${body.email}")
    }
}
