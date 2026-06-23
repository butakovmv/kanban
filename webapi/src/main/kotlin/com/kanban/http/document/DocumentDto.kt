package com.kanban.http.document

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class DocumentResponse(
    val id: String,
    @JsonProperty("project_id")
    val projectId: String,
    val path: String,
    val title: String,
    val description: String?,
    @JsonProperty("created_at")
    val createdAt: Instant,
    @JsonProperty("updated_at")
    val updatedAt: Instant,
)

data class DocumentDetailResponse(
    val id: String,
    @JsonProperty("project_id")
    val projectId: String,
    val path: String,
    val title: String,
    val content: String,
    val description: String?,
    @JsonProperty("created_at")
    val createdAt: Instant,
    @JsonProperty("updated_at")
    val updatedAt: Instant,
)
