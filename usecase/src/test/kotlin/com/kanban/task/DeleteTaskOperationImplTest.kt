package com.kanban.task

import com.kanban.common.BoardId
import com.kanban.common.ColumnId
import com.kanban.common.TaskId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DeleteTaskOperationImplTest {
    private val taskRepository = mockk<TaskRepository>()
    private val operation = DeleteTaskOperationImpl(taskRepository)

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
    fun `should delete existing task`() =
        runTest {
            coEvery { taskRepository.findById("task-1") } returns sampleTask
            coEvery { taskRepository.delete("task-1") } returns Unit

            val result = operation.execute(DeleteTaskOperation.Arg(taskId = "task-1"))

            assertIs<DeleteTaskOperation.Result.Success>(result)

            coVerify { taskRepository.findById("task-1") }
            coVerify { taskRepository.delete("task-1") }
        }

    @Test
    fun `should return NotFound when task not found`() =
        runTest {
            coEvery { taskRepository.findById("missing") } returns null

            val result = operation.execute(DeleteTaskOperation.Arg(taskId = "missing"))

            assertIs<DeleteTaskOperation.Result.NotFound>(result)

            coVerify { taskRepository.findById("missing") }
            coVerify(inverse = true) { taskRepository.delete(any()) }
        }
}
