package com.kanban.task

import com.kanban.common.CommentId
import com.kanban.common.TaskId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DeleteCommentOperationImplTest {
    private val commentRepository = mockk<CommentRepository>()
    private val operation = DeleteCommentOperationImpl(commentRepository)

    private val sampleComment =
        Comment(
            id = CommentId("comment-1"),
            taskId = TaskId("task-1"),
            authorId = "user-1",
            text = "Sample",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )

    @Test
    fun `should delete existing comment`() =
        runTest {
            coEvery { commentRepository.findById("comment-1") } returns sampleComment
            coEvery { commentRepository.delete("comment-1") } returns Unit

            val result = operation.execute(DeleteCommentOperation.Arg(commentId = "comment-1"))

            assertIs<DeleteCommentOperation.Result.Success>(result)

            coVerify { commentRepository.findById("comment-1") }
            coVerify { commentRepository.delete("comment-1") }
        }

    @Test
    fun `should return NotFound when comment not found`() =
        runTest {
            coEvery { commentRepository.findById("missing") } returns null

            val result = operation.execute(DeleteCommentOperation.Arg(commentId = "missing"))

            assertIs<DeleteCommentOperation.Result.NotFound>(result)

            coVerify { commentRepository.findById("missing") }
            coVerify(inverse = true) { commentRepository.delete(any()) }
        }
}
