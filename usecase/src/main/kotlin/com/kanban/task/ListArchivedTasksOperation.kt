package com.kanban.task

import com.kanban.common.Operation

interface ListArchivedTasksOperation : Operation<ListArchivedTasksOperation.Arg, ListArchivedTasksOperation.Result> {
    data class Arg(
        val projectId: String,
    )

    sealed interface Result {
        data class Success(
            val tasks: List<Task>,
        ) : Result
    }
}
