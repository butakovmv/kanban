package com.kanban.http.report

import com.kanban.report.GetCfdReportOperation
import com.kanban.report.GetLeadTimeReportOperation
import com.kanban.report.ReportHandler
import io.mockk.mockk
import org.springframework.test.web.reactive.server.WebTestClient

internal abstract class BaseReportControllerTest {
    protected lateinit var getCfdReportOperation: GetCfdReportOperation
    protected lateinit var getLeadTimeReportOperation: GetLeadTimeReportOperation
    protected lateinit var reportHandler: ReportHandler

    protected fun bindTo(controllerClass: Class<*>): WebTestClient {
        getCfdReportOperation = mockk()
        getLeadTimeReportOperation = mockk()
        reportHandler =
            ReportHandler(
                getCfdReportOperation = getCfdReportOperation,
                getLeadTimeReportOperation = getLeadTimeReportOperation,
            )

        val controller =
            when (controllerClass) {
                GetCfdReportController::class.java -> GetCfdReportController(reportHandler)
                GetLeadTimeReportController::class.java -> GetLeadTimeReportController(reportHandler)
                else -> throw IllegalArgumentException("Unsupported controller: $controllerClass")
            }

        return WebTestClient.bindToController(controller).build()
    }
}
