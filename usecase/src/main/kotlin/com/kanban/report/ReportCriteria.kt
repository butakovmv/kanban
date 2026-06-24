package com.kanban.report

import java.time.Instant

data class ReportCriteria(
    val projectId: String?,
    val fromDate: Instant,
    val toDate: Instant,
    val interval: Interval,
)

enum class Interval { DAY, WEEK, MONTH }
