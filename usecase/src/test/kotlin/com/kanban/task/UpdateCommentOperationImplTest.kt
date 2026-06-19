package com.kanban.task

import com.kanban.common.CommentId
import com.kanban.common.TaskId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class UpdateCommentOperationImplTest {
    private val commentRepository = mockk<CommentRepository>()
    private val operation = UpdateCommentOperationImpl(commentRepository)

    private val sampleComment =
        Comment(
            id = CommentId("comment-1"),
            taskId = TaskId("task-1"),
            authorId = "user-1",
            text = "Old text",
            createdAt = Instant.parse("2024-12-01T00:00:00Z"),
            updatedAt = Instant.parse("2024-12-01T00:00:00Z"),
        )

    @Test
    fun `should update comment text`() =
        runTest {
            coEvery { commentRepository.findById("comment-1") } returns sampleComment
            coEvery { commentRepository.save(any()) } answers { firstArg() }

            val result =
                operation.execute(
                    UpdateCommentOperation.Arg(commentId = "comment-1", text = "  New text  "),
                )

            val success = assertIs<UpdateCommentOperation.Result.Success>(result)
            assertEquals("New text", success.comment.text)
            assertNotEquals(sampleComment.updatedAt, success.comment.updatedAt)
            assertEquals(sampleComment.createdAt, success.comment.createdAt)

            coVerify { commentRepository.findById("comment-1") }
            coVerify { commentRepository.save(any()) }
        }

    @Test
    fun `should return NotFound when comment not found`() =
        runTest {
            coEvery { commentRepository.findById("missing") } returns null

            val result =
                operation.execute(
                    UpdateCommentOperation.Arg(commentId = "missing", text = "text"),
                )

            assertIs<UpdateCommentOperation.Result.NotFound>(result)

            coVerify { commentRepository.findById("missing") }
            coVerify(inverse = true) { commentRepository.save(any()) }
        }

    @Test
    fun `should fail when text is blank`() =
        runTest {
            val result =
                operation.execute(
                    UpdateCommentOperation.Arg(commentId = "comment-1", text = "  "),
                )

            val failure = assertIs<UpdateCommentOperation.Result.Failure>(result)
            assertEquals("Comment text must not be blank", failure.reason)

            coVerify(inverse = true) { commentRepository.findById(any()) }
            coVerify(inverse = true) { commentRepository.save(any()) }
        }
}
