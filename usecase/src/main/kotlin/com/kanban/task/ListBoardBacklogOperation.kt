package com.kanban.task

import com.kanban.common.Operation

interface ListBoardBacklogOperation : Operation<ListBoardBacklogOperation.Arg, ListBoardBacklogOperation.Result> {
    data class Arg(
        val projectId: String,
    )

    sealed interface Result {
        data class Success(
            val tasks: List<Task>,
        ) : Result
    }
}
