package com.kanban.http.auth

import com.kanban.identity.AuthHandler
import com.kanban.identity.LoginWithPasswordOperation
import com.kanban.identity.LogoutOperation
import com.kanban.identity.RefreshTokenOperation
import com.kanban.identity.RegisterUserOperation
import io.mockk.mockk
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Базовый класс для тестов контроллеров.
 * Создаёт WebTestClient с прямой привязкой к контроллерам.
 * Моки usecase-операций создаются автоматически и доступны как поля теста.
 *
 * Примечание: для snake_case сериализации используются @JsonProperty аннотации на DTO,
 * что позволяет работать с дефолтным ObjectMapper в тестовом окружении.
 */
internal abstract class BaseControllerTest {
    protected lateinit var registerUserOperation: RegisterUserOperation
    protected lateinit var loginWithPasswordOperation: LoginWithPasswordOperation
    protected lateinit var refreshTokenOperation: RefreshTokenOperation
    protected lateinit var logoutOperation: LogoutOperation

    /**
     * Создаёт WebTestClient привязанный к указанному контроллеру.
     * Моки usecase-операций создаются автоматически и доступны как поля теста.
     *
     * @param controllerClass класс контроллера для тестирования
     * @return настроенный WebTestClient
     */
    protected fun bindTo(controllerClass: Class<*>): WebTestClient {
        registerUserOperation = mockk()
        loginWithPasswordOperation = mockk()
        refreshTokenOperation = mockk()
        logoutOperation = mockk()

        val handler =
            AuthHandler(
                registerUserOperation = registerUserOperation,
                loginWithPasswordOperation = loginWithPasswordOperation,
                refreshTokenOperation = refreshTokenOperation,
                logoutOperation = logoutOperation,
            )

        val controller =
            when (controllerClass) {
                RegisterController::class.java -> RegisterController(handler)
                LoginController::class.java -> LoginController(handler)
                RefreshController::class.java -> RefreshController(handler)
                LogoutController::class.java -> LogoutController(handler)
                else -> throw IllegalArgumentException("Unsupported controller: $controllerClass")
            }

        return WebTestClient.bindToController(controller).build()
    }
}
