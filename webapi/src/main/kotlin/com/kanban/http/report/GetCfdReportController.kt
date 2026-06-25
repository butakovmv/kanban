package com.kanban.http.report

import com.kanban.report.Interval
import com.kanban.report.ReportHandler
import java.time.Instant
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/reports")
internal class GetCfdReportController(
    private val handler: ReportHandler,
) {
    @GetMapping("/cfd")
    suspend fun getCfd(
        @RequestParam("project_id") projectId: String?,
        @RequestParam("from") from: String,
        @RequestParam("to") to: String,
        @RequestParam("interval", defaultValue = "DAY") interval: String,
    ): ResponseEntity<*> {
        val result =
            handler.getCfd(
                projectId = projectId,
                from = Instant.parse(from),
                to = Instant.parse(to),
                interval = Interval.valueOf(interval.uppercase()),
            )
        return when (result) {
            is ReportHandler.CfdResult.Success ->
                ResponseEntity.ok(
                    mapOf(
                        "points" to
                            result.points.map {
                                CfdPointResponse(
                                    date = it.date,
                                    columnId = it.columnId,
                                    columnName = it.columnName,
                                    count = it.count,
                                )
                            },
                    ),
                )
        }
    }
}
