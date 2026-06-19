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
import kotlin.test.assertNotEquals
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class UpdateTaskOperationImplTest {
    private val taskRepository = mockk<TaskRepository>()
    private val operation = UpdateTaskOperationImpl(taskRepository)

    private val sampleTask =
        Task(
            id = TaskId("task-1"),
            boardId = BoardId("board-1"),
            columnId = ColumnId("column-1"),
            title = "Old Title",
            description = "Old Description",
            assigneeId = "user-1",
            position = 0,
            dueDate = Instant.parse("2025-01-01T00:00:00Z"),
            archived = false,
            createdAt = Instant.parse("2024-12-01T00:00:00Z"),
            updatedAt = Instant.parse("2024-12-01T00:00:00Z"),
        )

    @Test
    fun `should update all provided fields`() =
        runTest {
            coEvery { taskRepository.findById("task-1") } returns sampleTask
            coEvery { taskRepository.save(any()) } answers { firstArg() }

            val newDueDate = Instant.parse("2025-02-15T00:00:00Z")
            val result =
                operation.execute(
                    UpdateTaskOperation.Arg(
                        taskId = "task-1",
                        title = "  New Title  ",
                        description = "New Description",
                        assigneeId = "user-2",
                        dueDate = newDueDate,
                    ),
                )

            val success = assertIs<UpdateTaskOperation.Result.Success>(result)
            assertEquals("New Title", success.task.title)
            assertEquals("New Description", success.task.description)
            assertEquals("user-2", success.task.assigneeId)
            assertEquals(newDueDate, success.task.dueDate)
            assertNotEquals(sampleTask.updatedAt, success.task.updatedAt)

            coVerify { taskRepository.findById("task-1") }
            coVerify { taskRepository.save(any()) }
        }

    @Test
    fun `should keep existing values when arguments are null`() =
        runTest {
            coEvery { taskRepository.findById("task-1") } returns sampleTask
            coEvery { taskRepository.save(any()) } answers { firstArg() }

            val result =
                operation.execute(
                    UpdateTaskOperation.Arg(
                        taskId = "task-1",
                        title = null,
                        description = null,
                        assigneeId = null,
                        dueDate = null,
                    ),
                )

            val success = assertIs<UpdateTaskOperation.Result.Success>(result)
            assertEquals(sampleTask.title, success.task.title)
            assertEquals(sampleTask.description, success.task.description)
            assertEquals(sampleTask.assigneeId, success.task.assigneeId)
            assertEquals(sampleTask.dueDate, success.task.dueDate)
        }

    @Test
    fun `should return NotFound when task not found`() =
        runTest {
            coEvery { taskRepository.findById("missing") } returns null

            val result =
                operation.execute(
                    UpdateTaskOperation.Arg(
                        taskId = "missing",
                        title = "x",
                        description = null,
                        assigneeId = null,
                        dueDate = null,
                    ),
                )

            assertIs<UpdateTaskOperation.Result.NotFound>(result)

            coVerify { taskRepository.findById("missing") }
            coVerify(inverse = true) { taskRepository.save(any()) }
        }

    @Test
    fun `should fail when title is blank`() =
        runTest {
            coEvery { taskRepository.findById("task-1") } returns sampleTask

            val result =
                operation.execute(
                    UpdateTaskOperation.Arg(
                        taskId = "task-1",
                        title = "   ",
                        description = null,
                        assigneeId = null,
                        dueDate = null,
                    ),
                )

            val failure = assertIs<UpdateTaskOperation.Result.Failure>(result)
            assertEquals("Title must not be blank", failure.reason)

            coVerify { taskRepository.findById("task-1") }
            coVerify(inverse = true) { taskRepository.save(any()) }
        }
}
