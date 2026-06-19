package com.kanban.postgres.task

import com.kanban.common.BoardId
import com.kanban.common.ColumnId
import com.kanban.common.TaskId
import com.kanban.postgres.project.BoardGenerator
import com.kanban.postgres.project.ColumnGenerator
import com.kanban.postgres.project.ProjectGenerator
import com.kanban.task.Task
import com.kanban.task.TaskRepository
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
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
internal class TaskRepositoryImplTest {
    @Autowired
    private lateinit var db: DatabaseClient

    private lateinit var projectGenerator: ProjectGenerator
    private lateinit var boardGenerator: BoardGenerator
    private lateinit var columnGenerator: ColumnGenerator
    private lateinit var taskGenerator: TaskGenerator
    private lateinit var taskRepository: TaskRepository

    private lateinit var boardId: String
    private lateinit var columnId: String
    private lateinit var otherBoardId: String
    private lateinit var otherColumnId: String

    @BeforeEach
    fun setUp() =
        runTest {
            projectGenerator = ProjectGenerator(db)
            boardGenerator = BoardGenerator(db)
            columnGenerator = ColumnGenerator(db)
            taskGenerator = TaskGenerator(db)
            taskRepository = TaskRepositoryImpl(db)
            val projectId = projectGenerator.createAndInsert()
            boardId = boardGenerator.createAndInsert(projectId = projectId)
            columnId = columnGenerator.createAndInsert(boardId = boardId)
            val otherProjectId = projectGenerator.createAndInsert()
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
    fun `should save new task and find by id`() =
        runTest {
            val now = Instant.now()
            val task =
                Task(
                    id = TaskId("new-task-id"),
                    boardId = BoardId(boardId),
                    columnId = ColumnId(columnId),
                    title = "My task",
                    description = "Some desc",
                    assigneeId = "user-1",
                    position = 0,
                    dueDate = now.plusSeconds(3600),
                    archived = false,
                    createdAt = now,
                    updatedAt = now,
                )

            val saved = taskRepository.save(task)

            assertEquals("new-task-id", saved.id.value)
            assertEquals(boardId, saved.boardId.value)
            assertEquals(columnId, saved.columnId.value)

            val found = taskRepository.findById("new-task-id")
            assertNotNull(found)
            assertEquals("new-task-id", found.id.value)
            assertEquals(boardId, found.boardId.value)
            assertEquals(columnId, found.columnId.value)
            assertEquals("My task", found.title)
            assertEquals("Some desc", found.description)
            assertEquals("user-1", found.assigneeId)
            assertEquals(0, found.position)
            assertNotNull(found.dueDate)
            assertEquals(false, found.archived)
        }

    @Test
    fun `should save task with null optional fields`() =
        runTest {
            val now = Instant.now()
            val task =
                Task(
                    id = TaskId("null-task"),
                    boardId = BoardId(boardId),
                    columnId = ColumnId(columnId),
                    title = "No desc",
                    description = null,
                    assigneeId = null,
                    position = 0,
                    dueDate = null,
                    archived = false,
                    createdAt = now,
                    updatedAt = now,
                )

            taskRepository.save(task)

            val found = taskRepository.findById("null-task")
            assertNotNull(found)
            assertNull(found.description)
            assertNull(found.assigneeId)
            assertNull(found.dueDate)
        }

    @Test
    fun `should return null for unknown id`() =
        runTest {
            val found = taskRepository.findById("unknown-id")
            assertNull(found)
        }

    @Test
    fun `should update existing task`() =
        runTest {
            val taskId =
                taskGenerator.createAndInsert(
                    TaskSpec(boardId = boardId, columnId = columnId, title = "Old", position = 0),
                )
            val existing = taskRepository.findById(taskId)!!
            val updated =
                existing.copy(
                    title = "New",
                    description = "Added description",
                    position = 7,
                    assigneeId = "user-2",
                    dueDate = Instant.now().plusSeconds(7200),
                )

            val saved = taskRepository.save(updated)

            assertEquals("New", saved.title)
            val reloaded = taskRepository.findById(taskId)
            assertNotNull(reloaded)
            assertEquals("New", reloaded.title)
            assertEquals("Added description", reloaded.description)
            assertEquals(7, reloaded.position)
            assertEquals("user-2", reloaded.assigneeId)
            assertNotNull(reloaded.dueDate)
        }

    @Test
    fun `should clear nullable fields on update`() =
        runTest {
            val taskId =
                taskGenerator.createAndInsert(
                    TaskSpec(
                        boardId = boardId,
                        columnId = columnId,
                        description = "Old desc",
                        assigneeId = "user-1",
                    ),
                )
            val existing = taskRepository.findById(taskId)!!
            val updated = existing.copy(description = null, assigneeId = null)

            taskRepository.save(updated)

            val reloaded = taskRepository.findById(taskId)
            assertNotNull(reloaded)
            assertNull(reloaded.description)
            assertNull(reloaded.assigneeId)
        }

    @Test
    fun `should preserve archived flag on update`() =
        runTest {
            val taskId = taskGenerator.createAndInsert(TaskSpec(boardId = boardId, columnId = columnId))
            taskRepository.archive(taskId)
            val existing = taskRepository.findById(taskId)!!
            val updated = existing.copy(title = "Renamed after archive")

            taskRepository.save(updated)

            val reloaded = taskRepository.findById(taskId)
            assertNotNull(reloaded)
            assertEquals("Renamed after archive", reloaded.title)
            assertEquals(true, reloaded.archived)
        }

    @Test
    fun `should list tasks by board id ordered by position excluding archived`() =
        runTest {
            val firstId = taskGenerator.createAndInsert(TaskSpec(boardId = boardId, columnId = columnId, position = 0))
            val secondId = taskGenerator.createAndInsert(TaskSpec(boardId = boardId, columnId = columnId, position = 1))
            val thirdId = taskGenerator.createAndInsert(TaskSpec(boardId = boardId, columnId = columnId, position = 2))
            taskGenerator.createAndInsert(
                TaskSpec(boardId = boardId, columnId = columnId, position = 3, archived = true),
            )

            val list = taskRepository.listByBoardId(boardId)

            assertEquals(3, list.size)
            assertEquals(listOf(firstId, secondId, thirdId), list.map { it.id.value })
            assertEquals(listOf(0, 1, 2), list.map { it.position })
        }

    @Test
    fun `should list tasks by board id including archived when requested`() =
        runTest {
            taskGenerator.createAndInsert(TaskSpec(boardId = boardId, columnId = columnId, position = 0))
            val archivedId =
                taskGenerator.createAndInsert(
                    TaskSpec(boardId = boardId, columnId = columnId, position = 1, archived = true),
                )
            taskGenerator.createAndInsert(TaskSpec(boardId = boardId, columnId = columnId, position = 2))

            val list = taskRepository.listByBoardId(boardId, includeArchived = true)

            assertEquals(3, list.size)
            assertTrue(list.any { it.id.value == archivedId && it.archived })
        }

    @Test
    fun `should return empty list when board has no tasks`() =
        runTest {
            val list = taskRepository.listByBoardId(boardId)
            assertTrue(list.isEmpty())
        }

    @Test
    fun `should not include tasks from other boards`() =
        runTest {
            taskGenerator.createAndInsert(TaskSpec(boardId = boardId, columnId = columnId, position = 0))
            taskGenerator.createAndInsert(TaskSpec(boardId = otherBoardId, columnId = otherColumnId, position = 0))

            val list = taskRepository.listByBoardId(boardId)

            assertEquals(1, list.size)
            assertEquals(boardId, list.first().boardId.value)
        }

    @Test
    fun `should list tasks by column id ordered by position excluding archived`() =
        runTest {
            val firstId = taskGenerator.createAndInsert(TaskSpec(boardId = boardId, columnId = columnId, position = 0))
            val secondId = taskGenerator.createAndInsert(TaskSpec(boardId = boardId, columnId = columnId, position = 1))
            val thirdId = taskGenerator.createAndInsert(TaskSpec(boardId = boardId, columnId = columnId, position = 2))
            taskGenerator.createAndInsert(
                TaskSpec(boardId = boardId, columnId = columnId, position = 3, archived = true),
            )

            val list = taskRepository.listByColumnId(columnId)

            assertEquals(3, list.size)
            assertEquals(listOf(firstId, secondId, thirdId), list.map { it.id.value })
        }

    @Test
    fun `should return empty list when column has no tasks`() =
        runTest {
            val list = taskRepository.listByColumnId(columnId)
            assertTrue(list.isEmpty())
        }

    @Test
    fun `should delete task by id`() =
        runTest {
            val taskId = taskGenerator.createAndInsert(TaskSpec(boardId = boardId, columnId = columnId))
            assertNotNull(taskRepository.findById(taskId))

            taskRepository.delete(taskId)

            assertNull(taskRepository.findById(taskId))
        }

    @Test
    fun `should archive task by id`() =
        runTest {
            val taskId = taskGenerator.createAndInsert(TaskSpec(boardId = boardId, columnId = columnId))
            assertNotNull(taskRepository.findById(taskId))

            taskRepository.archive(taskId)

            val count =
                db
                    .sql("SELECT COUNT(*) AS cnt FROM tasks WHERE id = :id AND archived = TRUE")
                    .bind("id", taskId)
                    .map { row, _ -> (row.get("cnt", java.lang.Long::class.java) ?: 0L) as Long }
                    .one()
                    .awaitSingle()
            assertEquals(1L, count)
        }

    @Test
    fun `should reorder tasks by updating positions`() =
        runTest {
            val firstId = taskGenerator.createAndInsert(TaskSpec(boardId = boardId, columnId = columnId, position = 0))
            val secondId = taskGenerator.createAndInsert(TaskSpec(boardId = boardId, columnId = columnId, position = 1))
            val thirdId = taskGenerator.createAndInsert(TaskSpec(boardId = boardId, columnId = columnId, position = 2))

            val current = taskRepository.listByBoardId(boardId)
            val reordered =
                listOf(
                    current.first { it.id.value == thirdId },
                    current.first { it.id.value == firstId },
                    current.first { it.id.value == secondId },
                )

            taskRepository.updatePositions(reordered)

            val reloaded = taskRepository.listByBoardId(boardId)
            assertEquals(listOf(thirdId, firstId, secondId), reloaded.map { it.id.value })
            assertEquals(listOf(0, 1, 2), reloaded.map { it.position })
        }

    @Test
    fun `should move task between columns via updatePositions`() =
        runTest {
            val taskId = taskGenerator.createAndInsert(TaskSpec(boardId = boardId, columnId = columnId, position = 0))
            val existing = taskRepository.findById(taskId)!!
            val moved = existing.copy(columnId = ColumnId(otherColumnId), boardId = BoardId(otherBoardId))

            taskRepository.updatePositions(listOf(moved))

            val reloaded = taskRepository.findById(taskId)
            assertNotNull(reloaded)
            assertEquals(otherColumnId, reloaded.columnId.value)
            assertEquals(otherBoardId, reloaded.boardId.value)
            assertEquals(0, reloaded.position)
        }

    @Test
    fun `should handle empty list in updatePositions`() =
        runTest {
            taskRepository.updatePositions(emptyList())
        }

    @Test
    fun `should roundtrip timestamps through database`() =
        runTest {
            val nowLdt = LocalDateTime.now(ZoneId.systemDefault())
            val dueLdt = nowLdt.plusDays(2)
            val taskId =
                taskGenerator.createAndInsert(
                    TaskSpec(boardId = boardId, columnId = columnId, dueDate = dueLdt),
                )

            val found = taskRepository.findById(taskId)
            assertNotNull(found)
            assertNotNull(found.dueDate)
        }
}
