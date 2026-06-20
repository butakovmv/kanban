package com.kanban.search

import java.time.Instant

data class SearchCriteria(
    val query: String?,
    val projectId: String?,
    val boardId: String?,
    val status: String?,
    val priority: String?,
    val assigneeId: String?,
    val dueDateFrom: Instant?,
    val dueDateTo: Instant?,
    val limit: Int,
    val offset: Int,
)
