package com.kanban.report

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GetCfdReportOperationImplTest {
    private val reportRepository = mockk<ReportRepository>()
    private val operation = GetCfdReportOperationImpl(reportRepository)

    @Test
    fun `should return CFD data points from repository`() =
        runTest {
            val now = Instant.now()
            val criteria =
                ReportCriteria(
                    projectId = "project-1",
                    boardId = null,
                    fromDate = now.minusSeconds(86400 * 7),
                    toDate = now,
                    interval = Interval.DAY,
                )
            val expectedPoints =
                listOf(
                    CfdDataPoint(
                        date = now.minusSeconds(86400 * 2),
                        columnId = "col-1",
                        columnName = "To Do",
                        count = 3,
                    ),
                    CfdDataPoint(
                        date = now.minusSeconds(86400 * 1),
                        columnId = "col-2",
                        columnName = "In Progress",
                        count = 2,
                    ),
                )

            coEvery { reportRepository.getCfd(criteria) } returns expectedPoints

            val result = operation.execute(GetCfdReportOperation.Arg(criteria = criteria))

            val success = assertIs<GetCfdReportOperation.Result.Success>(result)
            assertEquals(expectedPoints, success.points)

            coVerify { reportRepository.getCfd(criteria) }
        }

    @Test
    fun `should return empty list when no data`() =
        runTest {
            val now = Instant.now()
            val criteria =
                ReportCriteria(
                    projectId = "project-1",
                    boardId = null,
                    fromDate = now.minusSeconds(86400),
                    toDate = now,
                    interval = Interval.DAY,
                )

            coEvery { reportRepository.getCfd(criteria) } returns emptyList()

            val result = operation.execute(GetCfdReportOperation.Arg(criteria = criteria))

            val success = assertIs<GetCfdReportOperation.Result.Success>(result)
            assertEquals(0, success.points.size)

            coVerify { reportRepository.getCfd(criteria) }
        }
}
