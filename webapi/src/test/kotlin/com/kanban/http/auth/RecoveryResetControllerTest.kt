package com.kanban.http.auth

import com.kanban.identity.RecoveryOperation
import io.mockk.coEvery
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера сброса пароля.
 * Проверяют корректность кодов ответа и обработки результатов операции.
 */
internal class RecoveryResetControllerTest : BaseControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(RecoveryResetController::class.java)
    }

    @Test
    fun `should return 200 on successful password reset`() {
        val request = RequestGenerator.resetPasswordRequest()

        coEvery {
            recoveryOperation.execute(any())
        } returns RecoveryOperation.Result.Success("Password reset successfully")

        webClient
            .post()
            .uri("/api/v1/auth/recovery/reset")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.message")
            .isEqualTo("Password reset successfully")
    }

    @Test
    fun `should return 400 on invalid token`() {
        val request = RequestGenerator.resetPasswordRequest()

        coEvery {
            recoveryOperation.execute(any())
        } returns RecoveryOperation.Result.Failure("Invalid or expired token")

        webClient
            .post()
            .uri("/api/v1/auth/recovery/reset")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.reason")
            .isEqualTo("Invalid or expired token")
    }

    @Test
    fun `should return 400 on empty new password`() {
        val request = RequestGenerator.resetPasswordRequest()

        coEvery {
            recoveryOperation.execute(any())
        } returns RecoveryOperation.Result.Failure("New password is required")

        webClient
            .post()
            .uri("/api/v1/auth/recovery/reset")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.reason")
            .isEqualTo("New password is required")
    }
}
