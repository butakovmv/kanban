package com.kanban.http.report

import com.kanban.report.CfdDataPoint
import com.kanban.report.GetCfdReportOperation
import io.mockk.coEvery
import io.mockk.coVerify
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

internal class GetCfdReportControllerTest : BaseReportControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(GetCfdReportController::class.java)
    }

    @Test
    fun `should return 200 with CFD points`() {
        val now = Instant.now()
        val points =
            listOf(
                CfdDataPoint(
                    date = now.minusSeconds(86400 * 2),
                    columnId = "col-1",
                    columnName = "To Do",
                    count = 3,
                ),
                CfdDataPoint(
                    date = now.minusSeconds(86400),
                    columnId = "col-2",
                    columnName = "In Progress",
                    count = 2,
                ),
            )

        coEvery {
            getCfdReportOperation.execute(any())
        } returns GetCfdReportOperation.Result.Success(points = points)

        webClient
            .get()
            .uri { builder ->
                builder
                    .path("/api/v1/reports/cfd")
                    .queryParam("project_id", "project-1")
                    .queryParam("from", now.minusSeconds(86400 * 7).toString())
                    .queryParam("to", now.toString())
                    .queryParam("interval", "DAY")
                    .build()
            }.accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.points.length()")
            .isEqualTo(2)
            .jsonPath("$.points[0].column_id")
            .isEqualTo("col-1")
            .jsonPath("$.points[0].column_name")
            .isEqualTo("To Do")
            .jsonPath("$.points[0].count")
            .isEqualTo(3)
            .jsonPath("$.points[1].column_id")
            .isEqualTo("col-2")

        coVerify {
            getCfdReportOperation.execute(any())
        }
    }

    @Test
    fun `should return 200 with empty points when no data`() {
        val now = Instant.now()

        coEvery {
            getCfdReportOperation.execute(any())
        } returns GetCfdReportOperation.Result.Success(points = emptyList())

        webClient
            .get()
            .uri { builder ->
                builder
                    .path("/api/v1/reports/cfd")
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

    @Test
    fun `should accept board_id filter`() {
        val now = Instant.now()

        coEvery {
            getCfdReportOperation.execute(any())
        } returns GetCfdReportOperation.Result.Success(points = emptyList())

        webClient
            .get()
            .uri { builder ->
                builder
                    .path("/api/v1/reports/cfd")
                    .queryParam("board_id", "board-1")
                    .queryParam("from", now.minusSeconds(86400).toString())
                    .queryParam("to", now.toString())
                    .build()
            }.accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk

        coVerify {
            getCfdReportOperation.execute(
                match { arg ->
                    arg.criteria.boardId == "board-1" &&
                        arg.criteria.projectId == null &&
                        arg.criteria.interval.name == "DAY"
                },
            )
        }
    }
}
