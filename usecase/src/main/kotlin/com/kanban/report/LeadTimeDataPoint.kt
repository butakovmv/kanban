package com.kanban.report

import java.time.Instant

data class LeadTimeDataPoint(
    val date: Instant,
    val taskId: String,
    val taskTitle: String,
    val leadTimeHours: Double,
)
