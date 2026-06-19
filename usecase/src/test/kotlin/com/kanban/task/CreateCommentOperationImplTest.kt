package com.kanban.task

import com.kanban.common.BoardId
import com.kanban.common.ColumnId
import com.kanban.common.TaskId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class CreateCommentOperationImplTest {
    private val commentRepository = mockk<CommentRepository>()
    private val taskRepository = mockk<TaskRepository>()
    private val operation = CreateCommentOperationImpl(commentRepository, taskRepository)

    private val sampleTask =
        Task(
            id = TaskId("task-1"),
            boardId = BoardId("board-1"),
            columnId = ColumnId("column-1"),
            title = "Task",
            description = null,
            assigneeId = null,
            position = 0,
            dueDate = null,
            archived = false,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )

    @Test
    fun `should create comment successfully`() =
        runTest {
            coEvery { taskRepository.findById("task-1") } returns sampleTask
            coEvery { commentRepository.save(any()) } answers { firstArg() }

            val result =
                operation.execute(
                    CreateCommentOperation.Arg(
                        taskId = "task-1",
                        authorId = "user-1",
                        text = "  Hello world  ",
                    ),
                )

            val success = assertIs<CreateCommentOperation.Result.Success>(result)
            assertEquals("Hello world", success.comment.text)
            assertEquals("user-1", success.comment.authorId)
            assertEquals(TaskId("task-1"), success.comment.taskId)
            assertEquals(success.comment.createdAt, success.comment.updatedAt)

            coVerify { taskRepository.findById("task-1") }
            coVerify { commentRepository.save(any()) }
        }

    @Test
    fun `should fail when text is blank`() =
        runTest {
            val result =
                operation.execute(
                    CreateCommentOperation.Arg(
                        taskId = "task-1",
                        authorId = "user-1",
                        text = "   ",
                    ),
                )

            val failure = assertIs<CreateCommentOperation.Result.Failure>(result)
            assertEquals("Comment text must not be blank", failure.reason)

            coVerify(inverse = true) { taskRepository.findById(any()) }
            coVerify(inverse = true) { commentRepository.save(any()) }
        }

    @Test
    fun `should fail when task not found`() =
        runTest {
            coEvery { taskRepository.findById("missing") } returns null

            val result =
                operation.execute(
                    CreateCommentOperation.Arg(
                        taskId = "missing",
                        authorId = "user-1",
                        text = "Comment",
                    ),
                )

            val failure = assertIs<CreateCommentOperation.Result.Failure>(result)
            assertEquals("Task not found", failure.reason)

            coVerify { taskRepository.findById("missing") }
            coVerify(inverse = true) { commentRepository.save(any()) }
        }
}
