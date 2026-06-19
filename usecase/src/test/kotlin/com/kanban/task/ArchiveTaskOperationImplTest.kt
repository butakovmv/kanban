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

class ArchiveTaskOperationImplTest {
    private val taskRepository = mockk<TaskRepository>()
    private val operation = ArchiveTaskOperationImpl(taskRepository)

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
    fun `should archive existing task`() =
        runTest {
            coEvery { taskRepository.findById("task-1") } returns sampleTask
            coEvery { taskRepository.archive("task-1") } returns Unit

            val result = operation.execute(ArchiveTaskOperation.Arg(taskId = "task-1"))

            assertIs<ArchiveTaskOperation.Result.Success>(result)

            coVerify { taskRepository.findById("task-1") }
            coVerify { taskRepository.archive("task-1") }
        }

    @Test
    fun `should return NotFound when task not found`() =
        runTest {
            coEvery { taskRepository.findById("missing") } returns null

            val result = operation.execute(ArchiveTaskOperation.Arg(taskId = "missing"))

            assertIs<ArchiveTaskOperation.Result.NotFound>(result)

            coVerify { taskRepository.findById("missing") }
            coVerify(inverse = true) { taskRepository.archive(any()) }
        }
}
