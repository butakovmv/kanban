package com.kanban.http.report

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class CfdPointResponse(
    val date: Instant,
    @JsonProperty("column_id")
    val columnId: String,
    @JsonProperty("column_name")
    val columnName: String,
    val count: Long,
)

data class LeadTimePointResponse(
    val date: Instant,
    @JsonProperty("task_id")
    val taskId: String,
    @JsonProperty("task_title")
    val taskTitle: String,
    @JsonProperty("lead_time_hours")
    val leadTimeHours: Double,
)
