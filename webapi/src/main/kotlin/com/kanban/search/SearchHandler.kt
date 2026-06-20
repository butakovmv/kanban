package com.kanban.search

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

internal class SearchHandler(
    private val searchOperation: SearchOperation,
) {
    suspend fun search(request: SearchRequest): SearchResultWrapper {
        val result =
            searchOperation.execute(
                SearchOperation.Arg(
                    criteria =
                        SearchCriteria(
                            query = request.query,
                            projectId = request.projectId,
                            boardId = request.boardId,
                            status = request.status,
                            priority = request.priority,
                            assigneeId = request.assigneeId,
                            dueDateFrom = request.dueDateFrom,
                            dueDateTo = request.dueDateTo,
                            limit = request.size,
                            offset = request.page * request.size,
                        ),
                ),
            )
        return when (result) {
            is SearchOperation.Result.Success ->
                SearchResultWrapper(
                    results = result.results.map { it.toResponse() },
                    total = result.total,
                )
        }
    }

    data class SearchRequest(
        val query: String?,
        @JsonProperty("project_id")
        val projectId: String?,
        @JsonProperty("board_id")
        val boardId: String?,
        val status: String?,
        val priority: String?,
        @JsonProperty("assignee_id")
        val assigneeId: String?,
        @JsonProperty("due_date_from")
        val dueDateFrom: Instant?,
        @JsonProperty("due_date_to")
        val dueDateTo: Instant?,
        val page: Int,
        val size: Int,
    )

    data class SearchResultWrapper(
        val results: List<SearchItemResponse>,
        val total: Long,
    )

    data class SearchItemResponse(
        val id: String,
        val title: String,
        val description: String?,
        val status: String,
        val priority: String?,
        @JsonProperty("assignee_id")
        val assigneeId: String?,
        @JsonProperty("board_id")
        val boardId: String,
        @JsonProperty("column_id")
        val columnId: String,
        @JsonProperty("project_id")
        val projectId: String,
        @JsonProperty("due_date")
        val dueDate: Instant?,
        @JsonProperty("created_at")
        val createdAt: Instant,
        @JsonProperty("updated_at")
        val updatedAt: Instant,
        val rank: Float,
    )

    private fun SearchResult.toResponse(): SearchItemResponse =
        SearchItemResponse(
            id = id,
            title = title,
            description = description,
            status = status,
            priority = priority,
            assigneeId = assigneeId,
            boardId = boardId,
            columnId = columnId,
            projectId = projectId,
            dueDate = dueDate,
            createdAt = createdAt,
            updatedAt = updatedAt,
            rank = rank,
        )
}
