package com.kanban.search

internal class SearchOperationImpl(
    private val searchRepository: SearchRepository,
) : SearchOperation {
    override suspend fun execute(arg: SearchOperation.Arg): SearchOperation.Result {
        val results = searchRepository.search(arg.criteria)
        val total = searchRepository.count(arg.criteria)
        return SearchOperation.Result.Success(
            results = results,
            total = total,
        )
    }
}
