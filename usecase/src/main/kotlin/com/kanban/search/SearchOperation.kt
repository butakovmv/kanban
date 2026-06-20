package com.kanban.search

import com.kanban.common.Operation

interface SearchOperation : Operation<SearchOperation.Arg, SearchOperation.Result> {
    data class Arg(
        val criteria: SearchCriteria,
    )

    sealed interface Result {
        data class Success(
            val results: List<SearchResult>,
            val total: Long,
        ) : Result
    }
}
