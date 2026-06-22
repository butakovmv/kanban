package com.kanban.project

import com.kanban.common.Operation

interface ListBoardsOperation : Operation<ListBoardsOperation.Arg, ListBoardsOperation.Result> {
    data class Arg(
        val projectId: String,
    )

    sealed interface Result {
        data class Success(
            val boards: List<Board>,
        ) : Result
    }
}
