package com.kanban.search

import java.time.Instant

internal class SearchHandler(
    private val searchOperation: SearchOperation,
) {
    data class SearchItemData(
        val id: String,
        val title: String,
        val description: String?,
        val status: String,
        val priority: String?,
        val assigneeId: String?,
        val columnId: String,
        val projectId: String,
        val boardId: String,
        val dueDate: Instant?,
        val createdAt: Instant,
        val updatedAt: Instant,
        val rank: Float,
    )

    data class SearchResultData(
        val results: List<SearchItemData>,
        val total: Long,
    )

    suspend fun search(
        query: String?,
        projectId: String?,
        status: String?,
        priority: String?,
        assigneeId: String?,
        dueDateFrom: Instant?,
        dueDateTo: Instant?,
        page: Int,
        size: Int,
    ): SearchResultData {
        val result =
            searchOperation.execute(
                SearchOperation.Arg(
                    criteria =
                        SearchCriteria(
                            query = query,
                            projectId = projectId,
                            status = status,
                            priority = priority,
                            assigneeId = assigneeId,
                            dueDateFrom = dueDateFrom,
                            dueDateTo = dueDateTo,
                            limit = size,
                            offset = page * size,
                        ),
                ),
            )
        return when (result) {
            is SearchOperation.Result.Success ->
                SearchResultData(
                    results = result.results.map { it.toData() },
                    total = result.total,
                )
        }
    }

    private fun SearchResult.toData(): SearchItemData =
        SearchItemData(
            id = id,
            title = title,
            description = description,
            status = status,
            priority = priority,
            assigneeId = assigneeId,
            columnId = columnId,
            projectId = projectId,
            boardId = boardId,
            dueDate = dueDate,
            createdAt = createdAt,
            updatedAt = updatedAt,
            rank = rank,
        )
}
