package com.kanban.report

import com.kanban.common.Operation

interface GetLeadTimeReportOperation : Operation<GetLeadTimeReportOperation.Arg, GetLeadTimeReportOperation.Result> {
    data class Arg(
        val criteria: ReportCriteria,
    )

    sealed interface Result {
        data class Success(
            val points: List<LeadTimeDataPoint>,
        ) : Result
    }
}
