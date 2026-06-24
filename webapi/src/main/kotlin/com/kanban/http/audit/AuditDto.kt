package com.kanban.http.audit

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "Audit log entry")
data class AuditEntryResponse(
    @field:Schema(description = "Entry ID", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: String,
    @JsonProperty("projectId")
    @field:Schema(description = "Project ID", example = "550e8400-e29b-41d4-a716-446655440001")
    val projectId: String?,
    @JsonProperty("documentId")
    @field:Schema(description = "Document ID", example = "550e8400-e29b-41d4-a716-446655440002", nullable = true)
    val documentId: String?,
    @JsonProperty("userId")
    @field:Schema(description = "User ID who performed the action", example = "550e8400-e29b-41d4-a716-446655440003")
    val userId: String,
    @field:Schema(description = "Action type", example = "task.created")
    val action: String,
    @field:Schema(description = "Additional details as JSON", example = "{\"task_id\":\"...\"}", nullable = true)
    val details: String?,
    @JsonProperty("createdAt")
    @field:Schema(description = "Timestamp when action occurred")
    val createdAt: Instant,
)

@Schema(description = "Audit log list response")
data class AuditLogListResponse(
    @field:Schema(description = "List of audit entries")
    val items: List<AuditEntryResponse>,
    @field:Schema(description = "Total number of entries")
    val total: Long,
)
