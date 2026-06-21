package com.kanban.postgres.search

import com.kanban.postgres.project.BoardGenerator
import com.kanban.postgres.project.ColumnGenerator
import com.kanban.postgres.project.ProjectGenerator
import com.kanban.search.SearchCriteria
import com.kanban.search.SearchRepository
import java.time.Instant
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
internal class SearchRepositoryImplTest {
    @Autowired
    private lateinit var db: DatabaseClient

    private lateinit var projectGenerator: ProjectGenerator
    private lateinit var boardGenerator: BoardGenerator
    private lateinit var columnGenerator: ColumnGenerator
    private lateinit var taskGenerator: TaskSearchGenerator
    private lateinit var searchRepository: SearchRepository

    private lateinit var boardId: String
    private lateinit var columnId: String
    private lateinit var projectId: String
    private lateinit var otherBoardId: String
    private lateinit var otherColumnId: String
    private lateinit var otherProjectId: String

    @BeforeEach
    fun setUp() =
        runTest {
            projectGenerator = ProjectGenerator(db)
            boardGenerator = BoardGenerator(db)
            columnGenerator = ColumnGenerator(db)
            taskGenerator = TaskSearchGenerator(db)
            searchRepository = TestSearchRepositoryImpl(db)

            projectId = projectGenerator.createAndInsert()
            boardId = boardGenerator.createAndInsert(projectId = projectId)
            columnId = columnGenerator.createAndInsert(boardId = boardId)

            otherProjectId = projectGenerator.createAndInsert()
            otherBoardId = boardGenerator.createAndInsert(projectId = otherProjectId)
            otherColumnId = columnGenerator.createAndInsert(boardId = otherBoardId)
        }

    @AfterEach
    fun tearDown() =
        runTest {
            taskGenerator.deleteAll()
            columnGenerator.deleteAll()
            boardGenerator.deleteAll()
            projectGenerator.deleteAll()
        }

    @Test
    fun `should find tasks by title query`() =
        runTest {
            taskGenerator.createAndInsert(
                TaskSearchSpec(
                    boardId = boardId,
                    columnId = columnId,
                    title = "Alpha release",
                    description = "First version",
                ),
            )
            taskGenerator.createAndInsert(
                TaskSearchSpec(
                    boardId = boardId,
                    columnId = columnId,
                    title = "Beta testing",
                    description = "Second phase",
                ),
            )
            taskGenerator.createAndInsert(
                TaskSearchSpec(
                    boardId = boardId,
                    columnId = columnId,
                    title = "Gamma feature",
                    description = "Third iteration",
                ),
            )

            val criteria =
                SearchCriteria(
                    query = "beta",
                    projectId = null,
                    boardId = null,
                    status = null,
                    priority = null,
                    assigneeId = null,
                    dueDateFrom = null,
                    dueDateTo = null,
                    limit = 20,
                    offset = 0,
                )

            val results = searchRepository.search(criteria)
            assertEquals(1, results.size)
            assertTrue(results[0].title.contains("Beta", ignoreCase = true))
        }

    @Test
    fun `should find tasks by description query`() =
        runTest {
            taskGenerator.createAndInsert(
                TaskSearchSpec(
                    boardId = boardId,
                    columnId = columnId,
                    title = "Main",
                    description = "This is a critical feature",
                ),
            )
            taskGenerator.createAndInsert(
                TaskSearchSpec(
                    boardId = boardId,
                    columnId = columnId,
                    title = "Other",
                    description = "Minor fix",
                ),
            )

            val criteria =
                SearchCriteria(
                    query = "critical",
                    projectId = null,
                    boardId = null,
                    status = null,
                    priority = null,
                    assigneeId = null,
                    dueDateFrom = null,
                    dueDateTo = null,
                    limit = 20,
                    offset = 0,
                )

            val results = searchRepository.search(criteria)
            assertEquals(1, results.size)
            assertEquals("Main", results[0].title)
        }

    @Test
    fun `should filter by board id`() =
        runTest {
            taskGenerator.createAndInsert(
                TaskSearchSpec(boardId = boardId, columnId = columnId, title = "Board A task"),
            )
            taskGenerator.createAndInsert(
                TaskSearchSpec(boardId = otherBoardId, columnId = otherColumnId, title = "Board B task"),
            )

            val criteria =
                SearchCriteria(
                    query = null,
                    projectId = null,
                    boardId = boardId,
                    status = null,
                    priority = null,
                    assigneeId = null,
                    dueDateFrom = null,
                    dueDateTo = null,
                    limit = 20,
                    offset = 0,
                )

            val results = searchRepository.search(criteria)
            assertEquals(1, results.size)
            assertEquals(boardId, results[0].boardId)
        }

    @Test
    fun `should filter by project id`() =
        runTest {
            taskGenerator.createAndInsert(
                TaskSearchSpec(boardId = boardId, columnId = columnId, title = "Project A task"),
            )
            taskGenerator.createAndInsert(
                TaskSearchSpec(boardId = otherBoardId, columnId = otherColumnId, title = "Project B task"),
            )

            val criteria =
                SearchCriteria(
                    query = null,
                    projectId = projectId,
                    boardId = null,
                    status = null,
                    priority = null,
                    assigneeId = null,
                    dueDateFrom = null,
                    dueDateTo = null,
                    limit = 20,
                    offset = 0,
                )

            val results = searchRepository.search(criteria)
            assertEquals(1, results.size)
            assertEquals(projectId, results[0].projectId)
        }

    @Test
    fun `should filter by assignee`() =
        runTest {
            taskGenerator.createAndInsert(
                TaskSearchSpec(
                    boardId = boardId,
                    columnId = columnId,
                    title = "User 1 task",
                    assigneeId = "00000000-0000-0000-0000-000000000001",
                ),
            )
            taskGenerator.createAndInsert(
                TaskSearchSpec(
                    boardId = boardId,
                    columnId = columnId,
                    title = "User 2 task",
                    assigneeId = "00000000-0000-0000-0000-000000000002",
                ),
            )

            val criteria =
                SearchCriteria(
                    query = null,
                    projectId = null,
                    boardId = null,
                    status = null,
                    priority = null,
                    assigneeId = "00000000-0000-0000-0000-000000000001",
                    dueDateFrom = null,
                    dueDateTo = null,
                    limit = 20,
                    offset = 0,
                )

            val results = searchRepository.search(criteria)
            assertEquals(1, results.size)
            assertEquals("00000000-0000-0000-0000-000000000001", results[0].assigneeId)
        }

    @Test
    fun `should filter by due date range`() =
        runTest {
            val z = ZoneId.systemDefault()
            val nowInstant = Instant.now()
            val now = nowInstant.atZone(z).toLocalDateTime()
            taskGenerator.createAndInsert(
                TaskSearchSpec(
                    boardId = boardId,
                    columnId = columnId,
                    title = "Past task",
                    dueDate = now.minusDays(5),
                ),
            )
            taskGenerator.createAndInsert(
                TaskSearchSpec(
                    boardId = boardId,
                    columnId = columnId,
                    title = "Current task",
                    dueDate = now.plusDays(1),
                ),
            )
            taskGenerator.createAndInsert(
                TaskSearchSpec(
                    boardId = boardId,
                    columnId = columnId,
                    title = "Future task",
                    dueDate = now.plusDays(10),
                ),
            )

            val criteria =
                SearchCriteria(
                    query = null,
                    projectId = null,
                    boardId = null,
                    status = null,
                    priority = null,
                    assigneeId = null,
                    dueDateFrom = nowInstant,
                    dueDateTo = nowInstant.plusSeconds(86400 * 5),
                    limit = 20,
                    offset = 0,
                )

            val results = searchRepository.search(criteria)
            assertEquals(1, results.size)
            assertEquals("Current task", results[0].title)
        }

    @Test
    fun `should paginate results`() =
        runTest {
            repeat(5) { i ->
                taskGenerator.createAndInsert(
                    TaskSearchSpec(boardId = boardId, columnId = columnId, title = "Task ${i + 1}"),
                )
            }

            val criteria =
                SearchCriteria(
                    query = null,
                    projectId = null,
                    boardId = null,
                    status = null,
                    priority = null,
                    assigneeId = null,
                    dueDateFrom = null,
                    dueDateTo = null,
                    limit = 2,
                    offset = 0,
                )

            val page1 = searchRepository.search(criteria)
            assertEquals(2, page1.size)

            val criteria2 = criteria.copy(offset = 2)
            val page2 = searchRepository.search(criteria2)
            assertEquals(2, page2.size)

            val allIds = page1.map { it.id } + page2.map { it.id }
            assertEquals(4, allIds.toSet().size)
        }

    @Test
    fun `should count total matching results`() =
        runTest {
            taskGenerator.createAndInsert(
                TaskSearchSpec(boardId = boardId, columnId = columnId, title = "Alpha feature"),
            )
            taskGenerator.createAndInsert(
                TaskSearchSpec(boardId = boardId, columnId = columnId, title = "Beta feature"),
            )
            taskGenerator.createAndInsert(
                TaskSearchSpec(boardId = boardId, columnId = columnId, title = "Gamma feature"),
            )

            val criteria =
                SearchCriteria(
                    query = "feature",
                    projectId = null,
                    boardId = null,
                    status = null,
                    priority = null,
                    assigneeId = null,
                    dueDateFrom = null,
                    dueDateTo = null,
                    limit = 1,
                    offset = 0,
                )

            val total = searchRepository.count(criteria)
            assertEquals(3L, total)
        }

    @Test
    fun `should exclude archived tasks`() =
        runTest {
            val now = LocalDateTime.now()
            val activeId =
                taskGenerator.createAndInsert(
                    TaskSearchSpec(boardId = boardId, columnId = columnId, title = "Active task"),
                )
            val archivedId = UUID.randomUUID().toString()
            db
                .sql(
                    """
                    INSERT INTO tasks (id, board_id, column_id, title, description,
                        assignee_id, position, due_date, archived, created_at, updated_at)
                    VALUES (:id, :boardId, :columnId, :title, :description,
                        :assigneeId, :position, :dueDate, :archived, :createdAt, :updatedAt)
                    """,
                ).bind("id", archivedId)
                .bind("boardId", boardId)
                .bind("columnId", columnId)
                .bind("title", "Archived task")
                .bindNull("description", String::class.java)
                .bindNull("assigneeId", String::class.java)
                .bind("position", 0)
                .bindNull("dueDate", LocalDateTime::class.java)
                .bind("archived", true)
                .bind("createdAt", now)
                .bind("updatedAt", now)
                .fetch()
                .rowsUpdated()
                .awaitSingle()

            val criteria =
                SearchCriteria(
                    query = null,
                    projectId = null,
                    boardId = null,
                    status = null,
                    priority = null,
                    assigneeId = null,
                    dueDateFrom = null,
                    dueDateTo = null,
                    limit = 20,
                    offset = 0,
                )

            val results = searchRepository.search(criteria)
            assertEquals(1, results.size)
            assertEquals(activeId, results[0].id)
        }

    @Test
    fun `should return empty list when nothing matches`() =
        runTest {
            taskGenerator.createAndInsert(
                TaskSearchSpec(boardId = boardId, columnId = columnId, title = "Something"),
            )

            val criteria =
                SearchCriteria(
                    query = "nonexistent",
                    projectId = null,
                    boardId = null,
                    status = null,
                    priority = null,
                    assigneeId = null,
                    dueDateFrom = null,
                    dueDateTo = null,
                    limit = 20,
                    offset = 0,
                )

            val results = searchRepository.search(criteria)
            assertTrue(results.isEmpty())
        }
}
