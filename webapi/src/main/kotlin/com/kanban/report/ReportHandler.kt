package com.kanban.report

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

internal class ReportHandler(
    private val getCfdReportOperation: GetCfdReportOperation,
    private val getLeadTimeReportOperation: GetLeadTimeReportOperation,
) {
    suspend fun getCfd(request: CfdRequest): CfdResult {
        val result =
            getCfdReportOperation.execute(
                GetCfdReportOperation.Arg(
                    criteria =
                        ReportCriteria(
                            projectId = request.projectId,
                            boardId = request.boardId,
                            fromDate = request.from,
                            toDate = request.to,
                            interval = request.interval,
                        ),
                ),
            )
        return when (result) {
            is GetCfdReportOperation.Result.Success ->
                CfdResult.Success(
                    points = result.points.map { it.toResponse() },
                )
        }
    }

    suspend fun getLeadTime(request: LeadTimeRequest): LeadTimeResult {
        val result =
            getLeadTimeReportOperation.execute(
                GetLeadTimeReportOperation.Arg(
                    criteria =
                        ReportCriteria(
                            projectId = request.projectId,
                            boardId = null,
                            fromDate = request.from,
                            toDate = request.to,
                            interval = Interval.DAY,
                        ),
                ),
            )
        return when (result) {
            is GetLeadTimeReportOperation.Result.Success ->
                LeadTimeResult.Success(
                    points = result.points.map { it.toResponse() },
                )
        }
    }

    data class CfdRequest(
        @JsonProperty("project_id")
        val projectId: String?,
        @JsonProperty("board_id")
        val boardId: String?,
        val from: Instant,
        val to: Instant,
        val interval: Interval,
    )

    data class LeadTimeRequest(
        @JsonProperty("project_id")
        val projectId: String?,
        val from: Instant,
        val to: Instant,
    )

    sealed interface CfdResult {
        data class Success(
            val points: List<CfdPointResponse>,
        ) : CfdResult
    }

    sealed interface LeadTimeResult {
        data class Success(
            val points: List<LeadTimePointResponse>,
        ) : LeadTimeResult
    }

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

    private fun CfdDataPoint.toResponse(): CfdPointResponse =
        CfdPointResponse(
            date = date,
            columnId = columnId,
            columnName = columnName,
            count = count,
        )

    private fun LeadTimeDataPoint.toResponse(): LeadTimePointResponse =
        LeadTimePointResponse(
            date = date,
            taskId = taskId,
            taskTitle = taskTitle,
            leadTimeHours = leadTimeHours,
        )
}
