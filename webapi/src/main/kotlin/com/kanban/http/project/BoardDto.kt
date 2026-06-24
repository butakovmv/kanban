package com.kanban.http.project

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class BoardResponse(
    val id: String,
    @JsonProperty("project_id")
    val projectId: String,
    val name: String,
    val position: Int,
    @JsonProperty("created_at")
    val createdAt: Instant,
)

data class ColumnResponse(
    val id: String,
    @JsonProperty("project_id")
    val projectId: String,
    val name: String,
    val position: Int,
    @JsonProperty("wip_limit")
    val wipLimit: Int?,
    @JsonProperty("created_at")
    val createdAt: Instant,
)

data class BoardViewResponse(
    val board: BoardResponse,
    val columns: List<ColumnResponse>,
)
