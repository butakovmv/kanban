package com.kanban.search

import java.time.Instant

data class SearchResult(
    val id: String,
    val title: String,
    val description: String?,
    val status: String,
    val priority: String?,
    val assigneeId: String?,
    val boardId: String,
    val columnId: String,
    val projectId: String,
    val dueDate: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val rank: Float,
)
