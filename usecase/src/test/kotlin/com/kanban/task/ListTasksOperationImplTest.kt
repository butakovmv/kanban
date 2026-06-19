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

class ListTasksOperationImplTest {
    private val taskRepository = mockk<TaskRepository>()
    private val operation = ListTasksOperationImpl(taskRepository)

    private fun task(
        id: String,
        position: Int,
        archived: Boolean = false,
    ): Task =
        Task(
            id = TaskId(id),
            boardId = BoardId("board-1"),
            columnId = ColumnId("column-1"),
            title = "Task $id",
            description = null,
            assigneeId = null,
            position = position,
            dueDate = null,
            archived = archived,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )

    @Test
    fun `should return tasks for board`() =
        runTest {
            val tasks = listOf(task("t-1", 0), task("t-2", 1))
            coEvery { taskRepository.listByBoardId("board-1", false) } returns tasks

            val result =
                operation.execute(
                    ListTasksOperation.Arg(boardId = "board-1", includeArchived = false),
                )

            val success = assertIs<ListTasksOperation.Result.Success>(result)
            assertEquals(tasks, success.tasks)

            coVerify { taskRepository.listByBoardId("board-1", false) }
        }

    @Test
    fun `should include archived tasks when requested`() =
        runTest {
            val tasks = listOf(task("t-1", 0), task("t-2", 1, archived = true))
            coEvery { taskRepository.listByBoardId("board-1", true) } returns tasks

            val result =
                operation.execute(
                    ListTasksOperation.Arg(boardId = "board-1", includeArchived = true),
                )

            val success = assertIs<ListTasksOperation.Result.Success>(result)
            assertEquals(2, success.tasks.size)
            assertEquals(true, success.tasks[1].archived)

            coVerify { taskRepository.listByBoardId("board-1", true) }
        }

    @Test
    fun `should return empty list when board has no tasks`() =
        runTest {
            coEvery { taskRepository.listByBoardId("empty", false) } returns emptyList()

            val result = operation.execute(ListTasksOperation.Arg(boardId = "empty"))

            val success = assertIs<ListTasksOperation.Result.Success>(result)
            assertEquals(emptyList(), success.tasks)
        }

    @Test
    fun `should default includeArchived to false`() =
        runTest {
            coEvery { taskRepository.listByBoardId("board-1", false) } returns emptyList()

            operation.execute(ListTasksOperation.Arg(boardId = "board-1"))

            coVerify { taskRepository.listByBoardId("board-1", false) }
        }
}
