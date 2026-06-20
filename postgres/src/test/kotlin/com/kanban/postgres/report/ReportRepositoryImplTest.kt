package com.kanban.postgres.report

import com.kanban.postgres.project.BoardGenerator
import com.kanban.postgres.project.ColumnGenerator
import com.kanban.postgres.project.ProjectGenerator
import com.kanban.report.Interval
import com.kanban.report.ReportCriteria
import com.kanban.report.ReportRepository
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.r2dbc.core.DatabaseClient

@DataR2dbcTest
internal class ReportRepositoryImplTest {
    @Autowired
    private lateinit var db: DatabaseClient

    private lateinit var projectGenerator: ProjectGenerator
    private lateinit var boardGenerator: BoardGenerator
    private lateinit var columnGenerator: ColumnGenerator
    private lateinit var reportRepository: ReportRepository

    private lateinit var projectId: String
    private lateinit var boardId: String
    private lateinit var columnId1: String
    private lateinit var columnId2: String
    private lateinit var columnName1: String
    private lateinit var columnName2: String

    @BeforeEach
    fun setUp() =
        runTest {
            projectGenerator = ProjectGenerator(db)
            boardGenerator = BoardGenerator(db)
            columnGenerator = ColumnGenerator(db)
            reportRepository = ReportRepositoryImpl(db)

            projectId = projectGenerator.createAndInsert()
            boardId = boardGenerator.createAndInsert(projectId = projectId)
            columnName1 = "To Do"
            columnName2 = "Done"
            columnId1 = columnGenerator.createAndInsert(boardId = boardId, name = columnName1)
            columnId2 = columnGenerator.createAndInsert(boardId = boardId, name = columnName2)
        }

    @AfterEach
    fun tearDown() =
        runTest {
            db
                .sql("DELETE FROM tasks")
                .fetch()
                .rowsUpdated()
                .awaitSingle()
            db
                .sql("DELETE FROM columns")
                .fetch()
                .rowsUpdated()
                .awaitSingle()
            db
                .sql("DELETE FROM boards")
                .fetch()
                .rowsUpdated()
                .awaitSingle()
            db
                .sql("DELETE FROM projects")
                .fetch()
                .rowsUpdated()
                .awaitSingle()
        }

    @Test
    fun `should return CFD data with correct counts per column and bucket`() =
        runTest {
            val z = ZoneId.systemDefault()
            val now = LocalDateTime.now()
            val day1 = now.minusDays(2)
            val day2 = now.minusDays(1)

            insertTask(columnId1, false, day1)
            insertTask(columnId1, false, day1)
            insertTask(columnId2, false, day1)
            insertTask(columnId1, false, day2)

            val from = day1.minusDays(1).atZone(z).toInstant()
            val to = now.plusDays(1).atZone(z).toInstant()

            val criteria =
                ReportCriteria(
                    projectId = projectId,
                    boardId = null,
                    fromDate = from,
                    toDate = to,
                    interval = Interval.DAY,
                )

            val result = reportRepository.getCfd(criteria)

            assertTrue(result.isNotEmpty())
            val day1Bucket = day1.toLocalDate().atStartOfDay(z).toInstant()
            val day2Bucket = day2.toLocalDate().atStartOfDay(z).toInstant()

            val day1Col1 = result.find { it.date == day1Bucket && it.columnId == columnId1 }
            val day1Col2 = result.find { it.date == day1Bucket && it.columnId == columnId2 }
            val day2Col1 = result.find { it.date == day2Bucket && it.columnId == columnId1 }

            assertEquals(2, day1Col1?.count)
            assertEquals(columnName1, day1Col1?.columnName)
            assertEquals(1, day1Col2?.count)
            assertEquals(columnName2, day1Col2?.columnName)
            assertEquals(1, day2Col1?.count)
        }

    @Test
    fun `should exclude archived tasks from CFD`() =
        runTest {
            val z = ZoneId.systemDefault()
            val now = LocalDateTime.now()
            val day1 = now.minusDays(1)

            insertTask(columnId1, false, day1)
            insertTask(columnId1, true, day1)

            val from = day1.minusDays(1).atZone(z).toInstant()
            val to = now.plusDays(1).atZone(z).toInstant()

            val criteria =
                ReportCriteria(
                    projectId = projectId,
                    boardId = null,
                    fromDate = from,
                    toDate = to,
                    interval = Interval.DAY,
                )

            val result = reportRepository.getCfd(criteria)

            val dayBucket = day1.toLocalDate().atStartOfDay(z).toInstant()
            val colCount = result.find { it.date == dayBucket && it.columnId == columnId1 }
            assertEquals(1, colCount?.count)
        }

    @Test
    fun `should return lead time for archived tasks`() =
        runTest {
            val z = ZoneId.systemDefault()
            val now = LocalDateTime.now()

            insertTask(columnId2, true, now.minusDays(5), now.minusDays(3))
            insertTask(columnId2, true, now.minusDays(4), now.minusDays(2))

            val from = now.minusDays(10).atZone(z).toInstant()
            val to = now.plusDays(1).atZone(z).toInstant()

            val criteria =
                ReportCriteria(
                    projectId = projectId,
                    boardId = null,
                    fromDate = from,
                    toDate = to,
                    interval = Interval.DAY,
                )

            val result = reportRepository.getLeadTime(criteria)

            assertEquals(2, result.size)
            result.forEach { point ->
                assertTrue(point.leadTimeHours > 0)
            }
        }

    @Test
    fun `should exclude non-archived tasks from lead time`() =
        runTest {
            val z = ZoneId.systemDefault()
            val now = LocalDateTime.now()

            insertTask(columnId2, true, now.minusDays(5), now.minusDays(3))
            insertTask(columnId1, false, now.minusDays(4))

            val from = now.minusDays(10).atZone(z).toInstant()
            val to = now.plusDays(1).atZone(z).toInstant()

            val criteria =
                ReportCriteria(
                    projectId = projectId,
                    boardId = null,
                    fromDate = from,
                    toDate = to,
                    interval = Interval.DAY,
                )

            val result = reportRepository.getLeadTime(criteria)

            assertEquals(1, result.size)
        }

    @Test
    fun `should return empty CFD when no tasks in range`() =
        runTest {
            val z = ZoneId.systemDefault()
            val from =
                LocalDateTime
                    .now()
                    .minusDays(10)
                    .atZone(z)
                    .toInstant()
            val to =
                LocalDateTime
                    .now()
                    .minusDays(9)
                    .atZone(z)
                    .toInstant()

            val criteria =
                ReportCriteria(
                    projectId = projectId,
                    boardId = null,
                    fromDate = from,
                    toDate = to,
                    interval = Interval.DAY,
                )

            val result = reportRepository.getCfd(criteria)
            assertTrue(result.isEmpty())
        }

    @Test
    fun `should return empty lead time when no archived tasks in range`() =
        runTest {
            val z = ZoneId.systemDefault()
            val now = LocalDateTime.now()
            insertTask(columnId1, false, now.minusDays(1))

            val from = now.minusDays(10).atZone(z).toInstant()
            val to = now.plusDays(1).atZone(z).toInstant()

            val criteria =
                ReportCriteria(
                    projectId = projectId,
                    boardId = null,
                    fromDate = from,
                    toDate = to,
                    interval = Interval.DAY,
                )

            val result = reportRepository.getLeadTime(criteria)
            assertTrue(result.isEmpty())
        }

    private suspend fun insertTask(
        columnId: String,
        archived: Boolean,
        createdAt: LocalDateTime,
        updatedAt: LocalDateTime? = null,
    ) {
        val id = UUID.randomUUID().toString()
        db
            .sql(
                """
                INSERT INTO tasks (id, board_id, column_id, title, description,
                    assignee_id, position, due_date, archived, created_at, updated_at)
                VALUES (:id, :boardId, :columnId, :title, :description,
                    :assigneeId, :position, :dueDate, :archived, :createdAt, :updatedAt)
                """,
            ).bind("id", id)
            .bind("boardId", boardId)
            .bind("columnId", columnId)
            .bind("title", "Task-$id")
            .bindNull("description", String::class.java)
            .bindNull("assigneeId", String::class.java)
            .bind("position", 0)
            .bindNull("dueDate", LocalDateTime::class.java)
            .bind("archived", archived)
            .bind("createdAt", createdAt)
            .bind("updatedAt", updatedAt ?: createdAt)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
