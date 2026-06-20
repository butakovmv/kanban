package com.kanban.report

internal class GetLeadTimeReportOperationImpl(
    private val reportRepository: ReportRepository,
) : GetLeadTimeReportOperation {
    override suspend fun execute(arg: GetLeadTimeReportOperation.Arg): GetLeadTimeReportOperation.Result {
        val points = reportRepository.getLeadTime(arg.criteria)
        return GetLeadTimeReportOperation.Result.Success(points)
    }
}
