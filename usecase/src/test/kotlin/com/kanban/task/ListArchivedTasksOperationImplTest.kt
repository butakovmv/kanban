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

class ListArchivedTasksOperationImplTest {
    private val taskRepository = mockk<TaskRepository>()
    private val operation = ListArchivedTasksOperationImpl(taskRepository)

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
    fun `should return only archived tasks for board`() =
        runTest {
            val archived1 = task("t-1", 0, archived = true)
            val archived2 = task("t-2", 1, archived = true)
            val nonArchived = task("t-3", 2)
            coEvery { taskRepository.listByBoardId("board-1", true) } returns
                listOf(archived1, archived2, nonArchived)

            val result = operation.execute(ListArchivedTasksOperation.Arg(boardId = "board-1"))

            val success = assertIs<ListArchivedTasksOperation.Result.Success>(result)
            assertEquals(2, success.tasks.size)
            assertEquals(true, success.tasks.all { it.archived })

            coVerify { taskRepository.listByBoardId("board-1", true) }
        }

    @Test
    fun `should return empty list when no archived tasks`() =
        runTest {
            val nonArchived = task("t-1", 0)
            coEvery { taskRepository.listByBoardId("board-1", true) } returns listOf(nonArchived)

            val result = operation.execute(ListArchivedTasksOperation.Arg(boardId = "board-1"))

            val success = assertIs<ListArchivedTasksOperation.Result.Success>(result)
            assertEquals(0, success.tasks.size)
        }

    @Test
    fun `should return empty list when board has no tasks`() =
        runTest {
            coEvery { taskRepository.listByBoardId("empty", true) } returns emptyList()

            val result = operation.execute(ListArchivedTasksOperation.Arg(boardId = "empty"))

            val success = assertIs<ListArchivedTasksOperation.Result.Success>(result)
            assertEquals(emptyList(), success.tasks)
        }
}
