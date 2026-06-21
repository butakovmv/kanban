package com.kanban.http.task

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class TaskResponse(
    val id: String,
    @JsonProperty("board_id")
    val boardId: String,
    @JsonProperty("column_id")
    val columnId: String,
    val title: String,
    val description: String?,
    @JsonProperty("assignee_id")
    val assigneeId: String?,
    val position: Int,
    @JsonProperty("due_date")
    val dueDate: Instant?,
    val archived: Boolean,
    @JsonProperty("created_at")
    val createdAt: Instant,
    @JsonProperty("updated_at")
    val updatedAt: Instant,
)
