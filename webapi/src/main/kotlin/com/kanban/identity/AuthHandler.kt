package com.kanban.identity

import com.kanban.audit.LogAuditEventOperation
import com.kanban.common.AuthTokens

internal class AuthHandler(
    private val registerUserOperation: RegisterUserOperation,
    private val loginWithPasswordOperation: LoginWithPasswordOperation,
    private val refreshTokenOperation: RefreshTokenOperation,
    private val logoutOperation: LogoutOperation,
    private val logAuditEventOperation: LogAuditEventOperation,
) {
    suspend fun register(
        email: String,
        password: String,
        displayName: String,
    ): AuthResult {
        val result =
            registerUserOperation.execute(
                RegisterUserOperation.Arg(
                    email = email,
                    password = password,
                    displayName = displayName,
                ),
            )
        return when (result) {
            is RegisterUserOperation.Result.Success -> {
                logAuditEventOperation.execute(
                    LogAuditEventOperation.Arg(
                        projectId = null,
                        documentId = null,
                        userId = result.user.id.value,
                        action = "user.registered",
                        details = "{\"email\":\"${result.user.email.value}\",\"display_name\":\"${result.user.displayName}\"}",
                    ),
                )
                AuthResult.Success(
                    accessToken = result.tokens.accessToken.value,
                    refreshToken = result.tokens.refreshToken.value,
                    userId = result.user.id.value,
                    email = result.user.email.value,
                    displayName = result.user.displayName,
                )
            }
            is RegisterUserOperation.Result.Failure ->
                AuthResult.Failure(reason = result.reason)
        }
    }

    suspend fun login(
        email: String,
        password: String,
    ): AuthResult {
        val result =
            loginWithPasswordOperation.execute(
                LoginWithPasswordOperation.Arg(
                    email = email,
                    password = password,
                ),
            )
        return when (result) {
            is LoginWithPasswordOperation.Result.Success ->
                AuthResult.Success(
                    accessToken = result.tokens.accessToken.value,
                    refreshToken = result.tokens.refreshToken.value,
                    userId = result.user.id.value,
                    email = result.user.email.value,
                    displayName = result.user.displayName,
                )
            is LoginWithPasswordOperation.Result.Failure ->
                AuthResult.Failure(reason = result.reason)
        }
    }

    suspend fun refresh(refreshToken: String): TokenResult {
        val result =
            refreshTokenOperation.execute(
                RefreshTokenOperation.Arg(refreshToken = refreshToken),
            )
        return when (result) {
            is RefreshTokenOperation.Result.Success ->
                TokenResult.Success(
                    accessToken = result.tokens.accessToken.value,
                    refreshToken = result.tokens.refreshToken.value,
                )
            is RefreshTokenOperation.Result.Failure ->
                TokenResult.Failure(reason = result.reason)
        }
    }

    suspend fun logout(refreshToken: String?) {
        val token = refreshToken ?: return
        logoutOperation.execute(LogoutOperation.Arg(refreshToken = token))
    }

    sealed interface AuthResult {
        data class Success(
            val accessToken: String,
            val refreshToken: String,
            val userId: String,
            val email: String,
            val displayName: String,
        ) : AuthResult

        data class Failure(
            val reason: String,
        ) : AuthResult
    }

    sealed interface TokenResult {
        data class Success(
            val accessToken: String,
            val refreshToken: String,
        ) : TokenResult

        data class Failure(
            val reason: String,
        ) : TokenResult
    }
}
