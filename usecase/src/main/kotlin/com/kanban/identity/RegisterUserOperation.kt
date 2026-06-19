package com.kanban.identity

import com.kanban.common.AuthTokens
import com.kanban.common.Operation

interface RegisterUserOperation : Operation<RegisterUserOperation.Arg, RegisterUserOperation.Result> {
    data class Arg(
        val email: String,
        val password: String,
        val displayName: String,
    )

    sealed interface Result {
        data class Success(
            val tokens: AuthTokens,
            val user: User,
        ) : Result

        data class Failure(
            val reason: String,
        ) : Result
    }
}
