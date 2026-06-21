package com.kanban.report

import java.time.Instant

internal class ReportHandler(
    private val getCfdReportOperation: GetCfdReportOperation,
    private val getLeadTimeReportOperation: GetLeadTimeReportOperation,
) {
    data class CfdPointData(
        val date: Instant,
        val columnId: String,
        val columnName: String,
        val count: Long,
    )

    data class LeadTimePointData(
        val date: Instant,
        val taskId: String,
        val taskTitle: String,
        val leadTimeHours: Double,
    )

    suspend fun getCfd(
        projectId: String?,
        boardId: String?,
        from: Instant,
        to: Instant,
        interval: Interval,
    ): CfdResult {
        val result =
            getCfdReportOperation.execute(
                GetCfdReportOperation.Arg(
                    criteria =
                        ReportCriteria(
                            projectId = projectId,
                            boardId = boardId,
                            fromDate = from,
                            toDate = to,
                            interval = interval,
                        ),
                ),
            )
        return when (result) {
            is GetCfdReportOperation.Result.Success ->
                CfdResult.Success(
                    points = result.points.map { it.toData() },
                )
        }
    }

    suspend fun getLeadTime(
        projectId: String?,
        from: Instant,
        to: Instant,
    ): LeadTimeResult {
        val result =
            getLeadTimeReportOperation.execute(
                GetLeadTimeReportOperation.Arg(
                    criteria =
                        ReportCriteria(
                            projectId = projectId,
                            boardId = null,
                            fromDate = from,
                            toDate = to,
                            interval = Interval.DAY,
                        ),
                ),
            )
        return when (result) {
            is GetLeadTimeReportOperation.Result.Success ->
                LeadTimeResult.Success(
                    points = result.points.map { it.toData() },
                )
        }
    }

    sealed interface CfdResult {
        data class Success(
            val points: List<CfdPointData>,
        ) : CfdResult
    }

    sealed interface LeadTimeResult {
        data class Success(
            val points: List<LeadTimePointData>,
        ) : LeadTimeResult
    }

    private fun CfdDataPoint.toData(): CfdPointData =
        CfdPointData(
            date = date,
            columnId = columnId,
            columnName = columnName,
            count = count,
        )

    private fun LeadTimeDataPoint.toData(): LeadTimePointData =
        LeadTimePointData(
            date = date,
            taskId = taskId,
            taskTitle = taskTitle,
            leadTimeHours = leadTimeHours,
        )
}
