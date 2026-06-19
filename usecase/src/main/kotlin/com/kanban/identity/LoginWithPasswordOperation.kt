package com.kanban.identity

import com.kanban.common.AuthTokens
import com.kanban.common.Operation

/**
 * Операция аутентификации пользователя по email и паролю.
 * При успешной проверке возвращает токены доступа и информацию о пользователе.
 */
interface LoginWithPasswordOperation : Operation<LoginWithPasswordOperation.Arg, LoginWithPasswordOperation.Result> {
    /**
     * Аргумент операции входа по паролю.
     *
     * @property email email-адрес пользователя
     * @property password пароль пользователя
     */
    data class Arg(
        val email: String,
        val password: String,
    )

    /**
     * Результат операции входа по паролю.
     */
    sealed interface Result {
        /** Аутентификация успешна. */
        data class Success(
            val tokens: AuthTokens,
            val user: User,
        ) : Result

        /** Ошибка аутентификации. */
        data class Failure(
            val reason: String,
        ) : Result
    }
}
