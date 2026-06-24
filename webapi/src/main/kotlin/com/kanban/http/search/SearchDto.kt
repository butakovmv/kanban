package com.kanban.http.search

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class SearchItemResponse(
    val id: String,
    val title: String,
    val description: String?,
    val status: String,
    val priority: String?,
    @JsonProperty("assignee_id")
    val assigneeId: String?,
    @JsonProperty("project_id")
    val projectId: String,
    @JsonProperty("column_id")
    val columnId: String,
    @JsonProperty("board_id")
    val boardId: String,
    @JsonProperty("due_date")
    val dueDate: Instant?,
    @JsonProperty("created_at")
    val createdAt: Instant,
    @JsonProperty("updated_at")
    val updatedAt: Instant,
    val rank: Float,
)

data class SearchResultWrapper(
    val results: List<SearchItemResponse>,
    val total: Long,
)
