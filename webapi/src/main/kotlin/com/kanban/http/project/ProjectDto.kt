package com.kanban.http.project

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "Project information")
data class ProjectResponse(
    @field:Schema(description = "Project unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: String,
    @JsonProperty("owner_id")
    @field:Schema(description = "Project owner user ID", example = "550e8400-e29b-41d4-a716-446655440001")
    val ownerId: String,
    @field:Schema(description = "Project name", example = "My Project")
    val name: String,
    @field:Schema(description = "Project description", example = "Project description", nullable = true)
    val description: String?,
    @JsonProperty("created_at")
    @field:Schema(description = "Creation timestamp")
    val createdAt: Instant,
    @JsonProperty("updated_at")
    @field:Schema(description = "Last update timestamp")
    val updatedAt: Instant,
)
