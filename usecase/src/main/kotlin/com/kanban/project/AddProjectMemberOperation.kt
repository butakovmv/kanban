package com.kanban.project

import com.kanban.common.Operation

interface AddProjectMemberOperation : Operation<AddProjectMemberOperation.Arg, AddProjectMemberOperation.Result> {
    data class Arg(
        val projectId: String,
        val userId: String,
    )

    sealed interface Result {
        data object Success : Result
        data object ProjectNotFound : Result
    }
}
