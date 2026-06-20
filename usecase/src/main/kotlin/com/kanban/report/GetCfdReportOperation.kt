package com.kanban.report

import com.kanban.common.Operation

interface GetCfdReportOperation : Operation<GetCfdReportOperation.Arg, GetCfdReportOperation.Result> {
    data class Arg(
        val criteria: ReportCriteria,
    )

    sealed interface Result {
        data class Success(
            val points: List<CfdDataPoint>,
        ) : Result
    }
}
