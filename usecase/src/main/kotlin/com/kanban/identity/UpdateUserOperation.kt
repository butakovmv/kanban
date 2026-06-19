package com.kanban.identity

import com.kanban.common.Operation

interface UpdateUserOperation : Operation<UpdateUserOperation.Arg, UpdateUserOperation.Result> {
    data class Arg(
        val userId: String,
        val displayName: String?,
        val email: String?,
    )

    sealed interface Result {
        data class Success(
            val user: User,
        ) : Result

        data object NotFound : Result

        data class Failure(
            val reason: String,
        ) : Result
    }
}
