package com.kanban.report

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
internal class ReportConfig {
    @Bean
    fun reportHandler(
        getCfdReportOperation: GetCfdReportOperation,
        getLeadTimeReportOperation: GetLeadTimeReportOperation,
    ): ReportHandler =
        ReportHandler(
            getCfdReportOperation = getCfdReportOperation,
            getLeadTimeReportOperation = getLeadTimeReportOperation,
        )
}
