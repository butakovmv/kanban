package com.kanban.http.report

import com.kanban.report.ReportHandler
import java.time.Instant
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/reports")
internal class GetLeadTimeReportController(
    private val handler: ReportHandler,
) {
    @GetMapping("/lead-time")
    suspend fun getLeadTime(
        @RequestParam("project_id") projectId: String?,
        @RequestParam("from") from: String,
        @RequestParam("to") to: String,
    ): ResponseEntity<*> {
        val result =
            handler.getLeadTime(
                projectId = projectId,
                from = Instant.parse(from),
                to = Instant.parse(to),
            )
        return when (result) {
            is ReportHandler.LeadTimeResult.Success ->
                ResponseEntity.ok(
                    mapOf(
                        "points" to result.points.map {
                            LeadTimePointResponse(
                                date = it.date,
                                taskId = it.taskId,
                                taskTitle = it.taskTitle,
                                leadTimeHours = it.leadTimeHours,
                            )
                        },
                    ),
                )
        }
    }
}
