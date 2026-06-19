package com.kanban.task

import com.kanban.common.CommentId
import com.kanban.common.TaskId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ListCommentsOperationImplTest {
    private val commentRepository = mockk<CommentRepository>()
    private val operation = ListCommentsOperationImpl(commentRepository)

    @Test
    fun `should return comments for task`() =
        runTest {
            val comments =
                listOf(
                    Comment(
                        id = CommentId("c-1"),
                        taskId = TaskId("task-1"),
                        authorId = "user-1",
                        text = "First",
                        createdAt = Instant.parse("2024-12-01T00:00:00Z"),
                        updatedAt = Instant.parse("2024-12-01T00:00:00Z"),
                    ),
                    Comment(
                        id = CommentId("c-2"),
                        taskId = TaskId("task-1"),
                        authorId = "user-2",
                        text = "Second",
                        createdAt = Instant.parse("2024-12-02T00:00:00Z"),
                        updatedAt = Instant.parse("2024-12-02T00:00:00Z"),
                    ),
                )
            coEvery { commentRepository.listByTaskId("task-1") } returns comments

            val result = operation.execute(ListCommentsOperation.Arg(taskId = "task-1"))

            val success = assertIs<ListCommentsOperation.Result.Success>(result)
            assertEquals(comments, success.comments)

            coVerify { commentRepository.listByTaskId("task-1") }
        }

    @Test
    fun `should return empty list when task has no comments`() =
        runTest {
            coEvery { commentRepository.listByTaskId("empty") } returns emptyList()

            val result = operation.execute(ListCommentsOperation.Arg(taskId = "empty"))

            val success = assertIs<ListCommentsOperation.Result.Success>(result)
            assertEquals(emptyList(), success.comments)
        }
}
