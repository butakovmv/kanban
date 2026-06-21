package com.kanban.http.project

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class ProjectResponse(
    val id: String,
    @JsonProperty("owner_id")
    val ownerId: String,
    val name: String,
    val description: String?,
    @JsonProperty("created_at")
    val createdAt: Instant,
    @JsonProperty("updated_at")
    val updatedAt: Instant,
)
