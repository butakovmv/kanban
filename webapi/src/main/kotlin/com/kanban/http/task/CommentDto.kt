package com.kanban.http.task

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class CommentResponse(
    val id: String,
    @JsonProperty("task_id")
    val taskId: String,
    @JsonProperty("author_id")
    val authorId: String,
    val text: String,
    @JsonProperty("created_at")
    val createdAt: Instant,
    @JsonProperty("updated_at")
    val updatedAt: Instant,
)
