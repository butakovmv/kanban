package com.kanban.http.auth

import com.kanban.identity.RecoveryOperation
import io.mockk.coEvery
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Тесты контроллера запроса восстановления пароля.
 * Проверяют корректность кодов ответа и обработки результатов операции.
 */
internal class RecoveryRequestControllerTest : BaseControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(RecoveryRequestController::class.java)
    }

    @Test
    fun `should return 200 on successful recovery request`() {
        val request = RequestGenerator.recoveryRequestBody()

        coEvery {
            recoveryOperation.execute(any())
        } returns RecoveryOperation.Result.Success("Recovery token sent")

        webClient
            .post()
            .uri("/api/v1/auth/recovery/request")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.message")
            .isEqualTo("Recovery token sent")
    }

    @Test
    fun `should return 200 with neutral message for non-existent email`() {
        val request = RequestGenerator.recoveryRequestBody()

        coEvery {
            recoveryOperation.execute(any())
        } returns RecoveryOperation.Result.Success("If the email exists, a recovery token has been sent")

        webClient
            .post()
            .uri("/api/v1/auth/recovery/request")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.message")
            .isEqualTo("If the email exists, a recovery token has been sent")
    }

    @Test
    fun `should return 400 on failure`() {
        val request = RequestGenerator.recoveryRequestBody()

        coEvery {
            recoveryOperation.execute(any())
        } returns RecoveryOperation.Result.Failure("Invalid email format")

        webClient
            .post()
            .uri("/api/v1/auth/recovery/request")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.reason")
            .isEqualTo("Invalid email format")
    }
}
