package com.kanban.identity

import com.kanban.common.Operation

data class UserDisplayInfo(
    val id: String,
    val displayName: String,
)

interface FindUsersOperation : Operation<FindUsersOperation.Arg, FindUsersOperation.Result> {
    data class Arg(
        val userIds: List<String>,
    )

    data class Result(
        val users: List<UserDisplayInfo>,
    )
}
