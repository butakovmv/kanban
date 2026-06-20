package com.kanban.report

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GetLeadTimeReportOperationImplTest {
    private val reportRepository = mockk<ReportRepository>()
    private val operation = GetLeadTimeReportOperationImpl(reportRepository)

    @Test
    fun `should return lead time data points from repository`() =
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

            coEvery { reportRepository.getLeadTime(criteria) } returns expectedPoints

            val result = operation.execute(GetLeadTimeReportOperation.Arg(criteria = criteria))

            val success = assertIs<GetLeadTimeReportOperation.Result.Success>(result)
            assertEquals(expectedPoints, success.points)

            coVerify { reportRepository.getLeadTime(criteria) }
        }

    @Test
    fun `should return empty list when no completed tasks`() =
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

            coEvery { reportRepository.getLeadTime(criteria) } returns emptyList()

            val result = operation.execute(GetLeadTimeReportOperation.Arg(criteria = criteria))

            val success = assertIs<GetLeadTimeReportOperation.Result.Success>(result)
            assertEquals(0, success.points.size)

            coVerify { reportRepository.getLeadTime(criteria) }
        }
}
