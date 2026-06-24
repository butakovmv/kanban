package com.kanban.project

import com.kanban.common.Operation

interface ListProjectMembersOperation : Operation<ListProjectMembersOperation.Arg, ListProjectMembersOperation.Result> {
    data class Arg(
        val projectId: String,
    )

    sealed interface Result {
        data class Success(
            val members: List<ProjectMember>,
        ) : Result
    }
}
