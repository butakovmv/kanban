package com.kanban.identity

internal class RecoveryHandler(
    private val recoveryOperation: RecoveryOperation,
) {
    suspend fun requestRecovery(email: String): RecoveryResult {
        val result =
            recoveryOperation.execute(
                RecoveryOperation.Arg(
                    email = email,
                    action = RecoveryOperation.Action.REQUEST,
                ),
            )
        return when (result) {
            is RecoveryOperation.Result.Success -> RecoveryResult.Success(message = result.message)
            is RecoveryOperation.Result.Failure -> RecoveryResult.Failure(reason = result.reason)
        }
    }

    suspend fun resetPassword(
        token: String,
        newPassword: String,
    ): RecoveryResult {
        val result =
            recoveryOperation.execute(
                RecoveryOperation.Arg(
                    email = "",
                    token = token,
                    newPassword = newPassword,
                    action = RecoveryOperation.Action.RESET,
                ),
            )
        return when (result) {
            is RecoveryOperation.Result.Success -> RecoveryResult.Success(message = result.message)
            is RecoveryOperation.Result.Failure -> RecoveryResult.Failure(reason = result.reason)
        }
    }

    sealed interface RecoveryResult {
        data class Success(
            val message: String,
        ) : RecoveryResult

        data class Failure(
            val reason: String,
        ) : RecoveryResult
    }
}
