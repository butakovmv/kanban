package com.kanban.identity

import com.kanban.common.Operation

interface GetUserOperation : Operation<GetUserOperation.Arg, GetUserOperation.Result> {
    data class Arg(
        val userId: String,
    )

    sealed interface Result {
        data class Success(
            val user: User,
        ) : Result

        data object NotFound : Result
    }
}
