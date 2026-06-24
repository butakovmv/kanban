package com.kanban.project

import com.kanban.common.Operation

interface ListMemberProjectsOperation : Operation<ListMemberProjectsOperation.Arg, ListMemberProjectsOperation.Result> {
    data class Arg(
        val userId: String,
    )

    sealed interface Result {
        data class Success(
            val projects: List<Project>,
        ) : Result
    }
}
