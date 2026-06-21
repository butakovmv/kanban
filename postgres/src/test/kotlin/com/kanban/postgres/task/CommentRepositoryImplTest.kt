package com.kanban.postgres.task

import com.kanban.common.CommentId
import com.kanban.common.TaskId
import com.kanban.postgres.project.BoardGenerator
import com.kanban.postgres.project.ColumnGenerator
import com.kanban.postgres.project.ProjectGenerator
import com.kanban.task.Comment
import com.kanban.task.CommentRepository
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.r2dbc.core.DatabaseClient

@DataR2dbcTest
internal class CommentRepositoryImplTest {
    @Autowired
    private lateinit var db: DatabaseClient

    private lateinit var projectGenerator: ProjectGenerator
    private lateinit var boardGenerator: BoardGenerator
    private lateinit var columnGenerator: ColumnGenerator
    private lateinit var taskGenerator: TaskGenerator
    private lateinit var commentGenerator: CommentGenerator
    private lateinit var commentRepository: CommentRepository

    private lateinit var taskId: String
    private lateinit var otherTaskId: String

    @BeforeEach
    fun setUp() =
        runTest {
            projectGenerator = ProjectGenerator(db)
            boardGenerator = BoardGenerator(db)
            columnGenerator = ColumnGenerator(db)
            taskGenerator = TaskGenerator(db)
            commentGenerator = CommentGenerator(db)
            commentRepository = CommentRepositoryImpl(db)
            val projectId = projectGenerator.createAndInsert()
            val boardId = boardGenerator.createAndInsert(projectId = projectId)
            val columnId = columnGenerator.createAndInsert(boardId = boardId)
            taskId = taskGenerator.createAndInsert(TaskSpec(boardId = boardId, columnId = columnId))
            otherTaskId = taskGenerator.createAndInsert(TaskSpec(boardId = boardId, columnId = columnId))
        }

    @AfterEach
    fun tearDown() =
        runTest {
            commentGenerator.deleteAll()
            taskGenerator.deleteAll()
            columnGenerator.deleteAll()
            boardGenerator.deleteAll()
            projectGenerator.deleteAll()
        }

    @Test
    fun `should save new comment and find by id`() =
        runTest {
            val now = Instant.now()
            val comment =
                Comment(
                    id = CommentId("00000000-0000-0000-0000-000000000111"),
                    taskId = TaskId(taskId),
                    authorId = "00000000-0000-0000-0000-000000000001",
                    text = "Hello world",
                    createdAt = now,
                    updatedAt = now,
                )

            val saved = commentRepository.save(comment)

            assertEquals("00000000-0000-0000-0000-000000000111", saved.id.value)
            val found = commentRepository.findById("00000000-0000-0000-0000-000000000111")
            assertNotNull(found)
            assertEquals("00000000-0000-0000-0000-000000000111", found.id.value)
            assertEquals(taskId, found.taskId.value)
            assertEquals("00000000-0000-0000-0000-000000000001", found.authorId)
            assertEquals("Hello world", found.text)
        }

    @Test
    fun `should return null for unknown id`() =
        runTest {
            val found = commentRepository.findById("00000000-0000-0000-0000-000000000105")
            assertNull(found)
        }

    @Test
    fun `should update existing comment`() =
        runTest {
            val commentId = commentGenerator.createAndInsert(taskId = taskId, text = "Old text")
            val existing = commentRepository.findById(commentId)!!
            val updated = existing.copy(text = "Updated text")

            val saved = commentRepository.save(updated)

            assertEquals("Updated text", saved.text)
            val reloaded = commentRepository.findById(commentId)
            assertNotNull(reloaded)
            assertEquals("Updated text", reloaded.text)
        }

    @Test
    fun `should list comments by task id ordered by created_at`() =
        runTest {
            val firstId = commentGenerator.createAndInsert(taskId = taskId, text = "First")
            val secondId = commentGenerator.createAndInsert(taskId = taskId, text = "Second")
            val thirdId = commentGenerator.createAndInsert(taskId = taskId, text = "Third")
            commentGenerator.createAndInsert(taskId = otherTaskId, text = "Other task")

            val list = commentRepository.listByTaskId(taskId)

            assertEquals(3, list.size)
            assertEquals(listOf(firstId, secondId, thirdId), list.map { it.id.value })
        }

    @Test
    fun `should return empty list when task has no comments`() =
        runTest {
            val list = commentRepository.listByTaskId(taskId)
            assertTrue(list.isEmpty())
        }

    @Test
    fun `should not include comments from other tasks`() =
        runTest {
            commentGenerator.createAndInsert(taskId = taskId, text = "Mine")
            commentGenerator.createAndInsert(taskId = otherTaskId, text = "Other")

            val list = commentRepository.listByTaskId(taskId)

            assertEquals(1, list.size)
            assertEquals("Mine", list.first().text)
        }

    @Test
    fun `should delete comment by id`() =
        runTest {
            val commentId = commentGenerator.createAndInsert(taskId = taskId)
            assertNotNull(commentRepository.findById(commentId))

            commentRepository.delete(commentId)

            assertNull(commentRepository.findById(commentId))
        }
}
