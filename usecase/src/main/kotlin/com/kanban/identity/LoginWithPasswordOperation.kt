package com.kanban.identity

import com.kanban.common.AuthTokens
import com.kanban.common.Operation

interface LoginWithPasswordOperation : Operation<LoginWithPasswordOperation.Arg, LoginWithPasswordOperation.Result> {
    data class Arg(
        val email: String,
        val password: String,
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
