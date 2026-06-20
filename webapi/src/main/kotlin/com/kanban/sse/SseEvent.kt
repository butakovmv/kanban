package com.kanban.sse

import java.time.Instant

internal data class SseEvent(
    val type: String,
    val data: String,
    val boardId: String?,
    val projectId: String?,
    val timestamp: Instant,
)
