package com.kanban.identity

import com.kanban.common.Operation

interface CreateUserOperation : Operation<CreateUserOperation.Arg, CreateUserOperation.Result> {
    data class Arg(
        val email: String,
        val passwordHash: String,
        val displayName: String,
    )

    sealed interface Result {
        data class Success(
            val user: User,
        ) : Result

        data class Failure(
            val reason: String,
        ) : Result
    }
}
