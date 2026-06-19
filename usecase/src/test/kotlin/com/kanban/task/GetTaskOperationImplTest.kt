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

class GetTaskOperationImplTest {
    private val taskRepository = mockk<TaskRepository>()
    private val operation = GetTaskOperationImpl(taskRepository)

    private val sampleTask =
        Task(
            id = TaskId("task-1"),
            boardId = BoardId("board-1"),
            columnId = ColumnId("column-1"),
            title = "Sample",
            description = null,
            assigneeId = null,
            position = 0,
            dueDate = null,
            archived = false,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )

    @Test
    fun `should return task when found`() =
        runTest {
            coEvery { taskRepository.findById("task-1") } returns sampleTask

            val result = operation.execute(GetTaskOperation.Arg(taskId = "task-1"))

            val success = assertIs<GetTaskOperation.Result.Success>(result)
            assertEquals(sampleTask, success.task)

            coVerify { taskRepository.findById("task-1") }
        }

    @Test
    fun `should return NotFound when task not found`() =
        runTest {
            coEvery { taskRepository.findById("missing") } returns null

            val result = operation.execute(GetTaskOperation.Arg(taskId = "missing"))

            assertIs<GetTaskOperation.Result.NotFound>(result)

            coVerify { taskRepository.findById("missing") }
        }
}
