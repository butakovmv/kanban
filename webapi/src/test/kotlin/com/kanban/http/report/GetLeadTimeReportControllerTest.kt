package com.kanban.http.report

import com.kanban.report.GetLeadTimeReportOperation
import com.kanban.report.LeadTimeDataPoint
import io.mockk.coEvery
import io.mockk.coVerify
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

internal class GetLeadTimeReportControllerTest : BaseReportControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(GetLeadTimeReportController::class.java)
    }

    @Test
    fun `should return 200 with lead time points`() {
        val now = Instant.now()
        val points =
            listOf(
                LeadTimeDataPoint(
                    date = now.minusSeconds(86400 * 3),
                    taskId = "task-1",
                    taskTitle = "Task 1",
                    leadTimeHours = 48.0,
                ),
                LeadTimeDataPoint(
                    date = now.minusSeconds(86400 * 2),
                    taskId = "task-2",
                    taskTitle = "Task 2",
                    leadTimeHours = 72.5,
                ),
            )

        coEvery {
            getLeadTimeReportOperation.execute(any())
        } returns GetLeadTimeReportOperation.Result.Success(points = points)

        webClient
            .get()
            .uri { builder ->
                builder
                    .path("/api/v1/reports/lead-time")
                    .queryParam("project_id", "project-1")
                    .queryParam("from", now.minusSeconds(86400 * 7).toString())
                    .queryParam("to", now.toString())
                    .build()
            }.accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.points.length()")
            .isEqualTo(2)
            .jsonPath("$.points[0].task_id")
            .isEqualTo("task-1")
            .jsonPath("$.points[0].task_title")
            .isEqualTo("Task 1")
            .jsonPath("$.points[0].lead_time_hours")
            .isEqualTo(48.0)
            .jsonPath("$.points[1].task_id")
            .isEqualTo("task-2")

        coVerify {
            getLeadTimeReportOperation.execute(any())
        }
    }

    @Test
    fun `should return 200 with empty points when no data`() {
        val now = Instant.now()

        coEvery {
            getLeadTimeReportOperation.execute(any())
        } returns GetLeadTimeReportOperation.Result.Success(points = emptyList())

        webClient
            .get()
            .uri { builder ->
                builder
                    .path("/api/v1/reports/lead-time")
                    .queryParam("project_id", "project-1")
                    .queryParam("from", now.minusSeconds(86400).toString())
                    .queryParam("to", now.toString())
                    .build()
            }.accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.points.length()")
            .isEqualTo(0)
    }
}
