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

class ListBoardBacklogOperationImplTest {
    private val taskRepository = mockk<TaskRepository>()
    private val operation = ListBoardBacklogOperationImpl(taskRepository)

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
    fun `should return non-archived tasks for board backlog`() =
        runTest {
            val tasks = listOf(task("t-1", 0), task("t-2", 1))
            coEvery { taskRepository.listByBoardId("board-1", false) } returns tasks

            val result = operation.execute(ListBoardBacklogOperation.Arg(boardId = "board-1"))

            val success = assertIs<ListBoardBacklogOperation.Result.Success>(result)
            assertEquals(tasks, success.tasks)

            coVerify { taskRepository.listByBoardId("board-1", false) }
        }

    @Test
    fun `should exclude archived tasks from backlog`() =
        runTest {
            val nonArchived = task("t-1", 0)
            val archived = task("t-2", 1, archived = true)
            coEvery { taskRepository.listByBoardId("board-1", false) } returns listOf(nonArchived)

            val result = operation.execute(ListBoardBacklogOperation.Arg(boardId = "board-1"))

            val success = assertIs<ListBoardBacklogOperation.Result.Success>(result)
            assertEquals(1, success.tasks.size)
            assertEquals(false, success.tasks[0].archived)

            coVerify { taskRepository.listByBoardId("board-1", false) }
        }

    @Test
    fun `should return empty list when board has no tasks`() =
        runTest {
            coEvery { taskRepository.listByBoardId("empty", false) } returns emptyList()

            val result = operation.execute(ListBoardBacklogOperation.Arg(boardId = "empty"))

            val success = assertIs<ListBoardBacklogOperation.Result.Success>(result)
            assertEquals(emptyList(), success.tasks)
        }
}
