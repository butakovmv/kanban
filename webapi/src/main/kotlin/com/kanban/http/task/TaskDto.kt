package com.kanban.http.task

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "Task response")
data class TaskResponse(
    @field:Schema(description = "Unique task identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: String,
    @JsonProperty("project_id")
    @field:Schema(description = "Project identifier", example = "550e8400-e29b-41d4-a716-446655440001")
    val projectId: String,
    @JsonProperty("column_id")
    @field:Schema(description = "Column identifier", example = "550e8400-e29b-41d4-a716-446655440002")
    val columnId: String,
    @field:Schema(description = "Task title", example = "Implement login feature")
    val title: String,
    @field:Schema(description = "Task description", example = "Add OAuth2 login support")
    val description: String?,
    @JsonProperty("assignee_id")
    @field:Schema(description = "Assignee user identifier", example = "550e8400-e29b-41d4-a716-446655440003", nullable = true)
    val assigneeId: String?,
    @field:Schema(description = "Position within column", example = "0")
    val position: Int,
    @JsonProperty("due_date")
    @field:Schema(description = "Due date", example = "2024-12-31T23:59:59Z", nullable = true)
    val dueDate: Instant?,
    @field:Schema(description = "Priority level", example = "high", nullable = true)
    val priority: String?,
    @field:Schema(description = "Whether task is archived", example = "false")
    val archived: Boolean,
    @JsonProperty("created_at")
    @field:Schema(description = "Creation timestamp")
    val createdAt: Instant,
    @JsonProperty("updated_at")
    @field:Schema(description = "Last update timestamp")
    val updatedAt: Instant,
)
