package com.kanban.identity

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.common.AuthTokens

/**
 * Обработчик auth-запросов.
 * Связывает HTTP-контроллеры с usecase-операциями: преобразует DTO в аргументы операций,
 * вызывает операции и преобразует результаты обратно в DTO.
 *
 * @property registerUserOperation операция регистрации
 * @property loginWithPasswordOperation операция входа по паролю
 * @property refreshTokenOperation операция обновления токена
 * @property logoutOperation операция выхода
 */
internal class AuthHandler(
    private val registerUserOperation: RegisterUserOperation,
    private val loginWithPasswordOperation: LoginWithPasswordOperation,
    private val refreshTokenOperation: RefreshTokenOperation,
    private val logoutOperation: LogoutOperation,
) {
    /**
     * Регистрация нового пользователя.
     *
     * @param request данные для регистрации
     * @return результат с токенами и пользователем или ошибка
     */
    suspend fun register(request: RegisterRequest): AuthResult {
        val result =
            registerUserOperation.execute(
                RegisterUserOperation.Arg(
                    email = request.email,
                    password = request.password,
                    displayName = request.displayName,
                ),
            )
        return when (result) {
            is RegisterUserOperation.Result.Success ->
                AuthResult.Success(
                    response = result.toResponse(),
                )
            is RegisterUserOperation.Result.Failure ->
                AuthResult.Failure(reason = result.reason)
        }
    }

    /**
     * Вход по паролю.
     *
     * @param request данные для входа
     * @return результат с токенами и пользователем или ошибка
     */
    suspend fun login(request: LoginRequest): AuthResult {
        val result =
            loginWithPasswordOperation.execute(
                LoginWithPasswordOperation.Arg(
                    email = request.email,
                    password = request.password,
                ),
            )
        return when (result) {
            is LoginWithPasswordOperation.Result.Success ->
                AuthResult.Success(
                    response = result.toResponse(),
                )
            is LoginWithPasswordOperation.Result.Failure ->
                AuthResult.Failure(reason = result.reason)
        }
    }

    /**
     * Обновление access-токена.
     *
     * @param request refresh-токен
     * @return результат с новыми токенами или ошибка
     */
    suspend fun refresh(request: RefreshRequest): TokenResult {
        val result =
            refreshTokenOperation.execute(
                RefreshTokenOperation.Arg(refreshToken = request.refreshToken),
            )
        return when (result) {
            is RefreshTokenOperation.Result.Success ->
                TokenResult.Success(
                    response = result.tokens.toResponse(),
                )
            is RefreshTokenOperation.Result.Failure ->
                TokenResult.Failure(reason = result.reason)
        }
    }

    /**
     * Выход из системы.
     *
     * @param request refresh-токен для аннулирования
     * @return результат выхода
     */
    suspend fun logout(request: LogoutRequest): LogoutResult {
        logoutOperation.execute(LogoutOperation.Arg(refreshToken = request.refreshToken))
        return LogoutResult.Success
    }

    /**
     * Преобразование результата регистрации в DTO ответа.
     */
    private fun RegisterUserOperation.Result.Success.toResponse(): AuthResponse =
        AuthResponse(
            accessToken = tokens.accessToken.value,
            refreshToken = tokens.refreshToken.value,
            user =
                UserResponse(
                    id = user.id.value,
                    email = user.email.value,
                    displayName = user.displayName,
                ),
        )

    /**
     * Преобразование результата входа в DTO ответа.
     */
    private fun LoginWithPasswordOperation.Result.Success.toResponse(): AuthResponse =
        AuthResponse(
            accessToken = tokens.accessToken.value,
            refreshToken = tokens.refreshToken.value,
            user =
                UserResponse(
                    id = user.id.value,
                    email = user.email.value,
                    displayName = user.displayName,
                ),
        )

    /**
     * Преобразование токенов в DTO ответа.
     */
    private fun AuthTokens.toResponse(): TokenResponse =
        TokenResponse(
            accessToken = accessToken.value,
            refreshToken = refreshToken.value,
        )

    /**
     * DTO запроса регистрации.
     *
     * @property email email-адрес пользователя
     * @property password пароль пользователя
     * @property displayName отображаемое имя
     */
    data class RegisterRequest(
        val email: String,
        val password: String,
        @JsonProperty("display_name")
        val displayName: String,
    )

    /**
     * DTO запроса входа.
     *
     * @property email email-адрес пользователя
     * @property password пароль пользователя
     */
    data class LoginRequest(
        val email: String,
        val password: String,
    )

    /**
     * DTO запроса обновления токена.
     *
     * @property refreshToken refresh-токен
     */
    data class RefreshRequest(
        @JsonProperty("refresh_token")
        val refreshToken: String,
    )

    /**
     * DTO запроса выхода.
     *
     * @property refreshToken refresh-токен для аннулирования
     */
    data class LogoutRequest(
        @JsonProperty("refresh_token")
        val refreshToken: String,
    )

    /**
     * DTO ответа с токенами и пользователем.
     *
     * @property accessToken access-токен
     * @property refreshToken refresh-токен
     * @property user информация о пользователе
     */
    data class AuthResponse(
        @JsonProperty("access_token")
        val accessToken: String,
        @JsonProperty("refresh_token")
        val refreshToken: String,
        val user: UserResponse,
    )

    /**
     * DTO ответа с новыми токенами.
     *
     * @property accessToken access-токен
     * @property refreshToken refresh-токен
     */
    data class TokenResponse(
        @JsonProperty("access_token")
        val accessToken: String,
        @JsonProperty("refresh_token")
        val refreshToken: String,
    )

    /**
     * DTO пользователя в ответе.
     *
     * @property id идентификатор пользователя
     * @property email email-адрес
     * @property displayName отображаемое имя
     */
    data class UserResponse(
        val id: String,
        val email: String,
        @JsonProperty("display_name")
        val displayName: String,
    )

    /**
     * Результат auth-операции (регистрация, вход).
     */
    sealed interface AuthResult {
        /** Успешный результат. */
        data class Success(
            val response: AuthResponse,
        ) : AuthResult

        /** Ошибка. */
        data class Failure(
            val reason: String,
        ) : AuthResult
    }

    /**
     * Результат операции обновления токена.
     */
    sealed interface TokenResult {
        /** Успешный результат. */
        data class Success(
            val response: TokenResponse,
        ) : TokenResult

        /** Ошибка. */
        data class Failure(
            val reason: String,
        ) : TokenResult
    }

    /**
     * Результат операции выхода.
     */
    sealed interface LogoutResult {
        /** Выход выполнен. */
        data object Success : LogoutResult
    }
}
