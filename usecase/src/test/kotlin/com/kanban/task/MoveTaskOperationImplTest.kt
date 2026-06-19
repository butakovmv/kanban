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

class MoveTaskOperationImplTest {
    private val taskRepository = mockk<TaskRepository>()
    private val operation = MoveTaskOperationImpl(taskRepository)

    private fun task(
        id: String,
        columnId: String,
        position: Int,
    ): Task =
        Task(
            id = TaskId(id),
            boardId = BoardId("board-1"),
            columnId = ColumnId(columnId),
            title = "Task $id",
            description = null,
            assigneeId = null,
            position = position,
            dueDate = null,
            archived = false,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )

    @Test
    fun `should move task to another column and renumber positions`() =
        runTest {
            val sourceTasks = listOf(task("t-1", "col-A", 0), task("t-2", "col-A", 1))
            val targetTasks = listOf(task("t-3", "col-B", 0), task("t-4", "col-B", 1), task("t-5", "col-B", 2))
            val movedTask = task("t-1", "col-A", 0)

            coEvery { taskRepository.findById("t-1") } returns movedTask
            coEvery { taskRepository.listByColumnId("col-A") } returns sourceTasks
            coEvery { taskRepository.listByColumnId("col-B") } returns targetTasks
            coEvery { taskRepository.updatePositions(any()) } returns Unit

            val result =
                operation.execute(
                    MoveTaskOperation.Arg(taskId = "t-1", columnId = "col-B", position = 1),
                )

            val success = assertIs<MoveTaskOperation.Result.Success>(result)
            assertEquals(ColumnId("col-B"), success.task.columnId)
            assertEquals(1, success.task.position)

            coVerify {
                taskRepository.updatePositions(
                    match { list ->
                        val target = list.filter { it.columnId.value == "col-B" }
                        val source = list.filter { it.columnId.value == "col-A" }
                        target.size == 4 &&
                            target.map { it.position } == listOf(0, 1, 2, 3) &&
                            target[1].id.value == "t-1" &&
                            source.size == 1 &&
                            source[0].id.value == "t-2" &&
                            source[0].position == 0
                    },
                )
            }
        }

    @Test
    fun `should reorder within same column`() =
        runTest {
            val tasks = listOf(task("t-1", "col-A", 0), task("t-2", "col-A", 1), task("t-3", "col-A", 2))
            val movedTask = task("t-3", "col-A", 2)

            coEvery { taskRepository.findById("t-3") } returns movedTask
            coEvery { taskRepository.listByColumnId("col-A") } returns tasks
            coEvery { taskRepository.updatePositions(any()) } returns Unit

            val result =
                operation.execute(
                    MoveTaskOperation.Arg(taskId = "t-3", columnId = "col-A", position = 0),
                )

            val success = assertIs<MoveTaskOperation.Result.Success>(result)
            assertEquals(0, success.task.position)

            coVerify {
                taskRepository.updatePositions(
                    match { list ->
                        val ids = list.filter { it.columnId.value == "col-A" }.map { it.id.value }
                        ids == listOf("t-3", "t-1", "t-2")
                    },
                )
            }
        }

    @Test
    fun `should clamp position to bounds when out of range`() =
        runTest {
            val sourceTasks = listOf(task("t-1", "col-A", 0))
            val targetTasks = listOf(task("t-2", "col-B", 0))
            val movedTask = task("t-1", "col-A", 0)

            coEvery { taskRepository.findById("t-1") } returns movedTask
            coEvery { taskRepository.listByColumnId("col-A") } returns sourceTasks
            coEvery { taskRepository.listByColumnId("col-B") } returns targetTasks
            coEvery { taskRepository.updatePositions(any()) } returns Unit

            val result =
                operation.execute(
                    MoveTaskOperation.Arg(taskId = "t-1", columnId = "col-B", position = 100),
                )

            val success = assertIs<MoveTaskOperation.Result.Success>(result)

            coVerify {
                taskRepository.updatePositions(
                    match { list ->
                        val target = list.filter { it.columnId.value == "col-B" }
                        target.size == 2 &&
                            target.last().id.value == "t-1" &&
                            target.last().position == 1
                    },
                )
            }
            assertEquals(100, success.task.position)
        }

    @Test
    fun `should return NotFound when task not found`() =
        runTest {
            coEvery { taskRepository.findById("missing") } returns null

            val result =
                operation.execute(
                    MoveTaskOperation.Arg(taskId = "missing", columnId = "col-B", position = 0),
                )

            assertIs<MoveTaskOperation.Result.NotFound>(result)

            coVerify { taskRepository.findById("missing") }
            coVerify(inverse = true) { taskRepository.updatePositions(any()) }
        }
}
