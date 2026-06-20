package com.kanban.report

internal class GetCfdReportOperationImpl(
    private val reportRepository: ReportRepository,
) : GetCfdReportOperation {
    override suspend fun execute(arg: GetCfdReportOperation.Arg): GetCfdReportOperation.Result {
        val points = reportRepository.getCfd(arg.criteria)
        return GetCfdReportOperation.Result.Success(points)
    }
}
