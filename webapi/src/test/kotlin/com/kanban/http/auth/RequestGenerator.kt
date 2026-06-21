package com.kanban.http.auth

import java.util.UUID

internal object RequestGenerator {
    fun registerBody(): RegisterBody =
        RegisterBody(
            email = "user-${UUID.randomUUID().toString().take(8)}@kanban.test",
            password = "secure-pwd-${UUID.randomUUID().toString().take(6)}",
            displayName = "Test User ${UUID.randomUUID().toString().take(6)}",
        )

    fun loginBody(): LoginBody =
        LoginBody(
            email = "user-${UUID.randomUUID().toString().take(8)}@kanban.test",
            password = "secure-pwd-${UUID.randomUUID().toString().take(6)}",
        )

    fun refreshBody(): RefreshBody =
        RefreshBody(
            refreshToken = "refresh-token-${UUID.randomUUID()}",
        )

    fun logoutBody(): LogoutBody =
        LogoutBody(
            refreshToken = "refresh-token-${UUID.randomUUID()}",
        )

    fun recoveryRequestBody(): RecoveryRequestBody =
        RecoveryRequestBody(
            email = "user-${UUID.randomUUID().toString().take(8)}@kanban.test",
        )

    fun resetPasswordBody(): ResetPasswordBody =
        ResetPasswordBody(
            token = "recovery-token-${UUID.randomUUID()}",
            newPassword = "new-pwd-${UUID.randomUUID().toString().take(6)}",
        )
}
