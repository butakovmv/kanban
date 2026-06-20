package com.kanban.report

interface ReportRepository {
    suspend fun getCfd(criteria: ReportCriteria): List<CfdDataPoint>

    suspend fun getLeadTime(criteria: ReportCriteria): List<LeadTimeDataPoint>
}
