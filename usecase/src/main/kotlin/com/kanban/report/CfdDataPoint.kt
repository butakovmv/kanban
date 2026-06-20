package com.kanban.report

import java.time.Instant

data class CfdDataPoint(
    val date: Instant,
    val columnId: String,
    val columnName: String,
    val count: Long,
)
