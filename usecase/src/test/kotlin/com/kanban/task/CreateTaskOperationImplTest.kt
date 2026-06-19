package com.kanban.task

import com.kanban.common.BoardId
import com.kanban.common.ColumnId
import com.kanban.common.ProjectId
import com.kanban.project.Board
import com.kanban.project.BoardRepository
import com.kanban.project.Column
import com.kanban.project.ColumnRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class CreateTaskOperationImplTest {
    private val taskRepository = mockk<TaskRepository>()
    private val boardRepository = mockk<BoardRepository>()
    private val columnRepository = mockk<ColumnRepository>()
    private val operation =
        CreateTaskOperationImpl(taskRepository, boardRepository, columnRepository)

    private val sampleBoard =
        Board(
            id = BoardId("board-1"),
            projectId = ProjectId("project-1"),
            name = "Main",
            position = 0,
            createdAt = Instant.now(),
        )

    private val sampleColumn =
        Column(
            id = ColumnId("column-1"),
            boardId = BoardId("board-1"),
            name = "To Do",
            position = 0,
            wipLimit = null,
            createdAt = Instant.now(),
        )

    @Test
    fun `should create task at end of column`() =
        runTest {
            coEvery { boardRepository.findById("board-1") } returns sampleBoard
            coEvery { columnRepository.findById("column-1") } returns sampleColumn
            coEvery { taskRepository.listByColumnId("column-1") } returns
                listOf(
                    sampleTask("task-0", position = 0),
                    sampleTask("task-1", position = 1),
                )
            coEvery { taskRepository.save(any()) } answers { firstArg() }

            val result =
                operation.execute(
                    CreateTaskOperation.Arg(
                        boardId = "board-1",
                        columnId = "column-1",
                        title = "  New Task  ",
                        description = "Description",
                        assigneeId = "user-1",
                        dueDate = null,
                    ),
                )

            val success = assertIs<CreateTaskOperation.Result.Success>(result)
            assertEquals("New Task", success.task.title)
            assertEquals(2, success.task.position)
            assertEquals(BoardId("board-1"), success.task.boardId)
            assertEquals(ColumnId("column-1"), success.task.columnId)
            assertEquals("user-1", success.task.assigneeId)
            assertEquals(false, success.task.archived)
            assertEquals(success.task.createdAt, success.task.updatedAt)

            coVerify { boardRepository.findById("board-1") }
            coVerify { columnRepository.findById("column-1") }
            coVerify { taskRepository.listByColumnId("column-1") }
            coVerify { taskRepository.save(any()) }
        }

    @Test
    fun `should fail when title is blank`() =
        runTest {
            val result =
                operation.execute(
                    CreateTaskOperation.Arg(
                        boardId = "board-1",
                        columnId = "column-1",
                        title = "   ",
                        description = null,
                        assigneeId = null,
                        dueDate = null,
                    ),
                )

            val failure = assertIs<CreateTaskOperation.Result.Failure>(result)
            assertEquals("Title must not be blank", failure.reason)

            coVerify(inverse = true) { boardRepository.findById(any()) }
            coVerify(inverse = true) { taskRepository.save(any()) }
        }

    @Test
    fun `should fail when board not found`() =
        runTest {
            coEvery { boardRepository.findById("missing") } returns null

            val result =
                operation.execute(
                    CreateTaskOperation.Arg(
                        boardId = "missing",
                        columnId = "column-1",
                        title = "Task",
                        description = null,
                        assigneeId = null,
                        dueDate = null,
                    ),
                )

            val failure = assertIs<CreateTaskOperation.Result.Failure>(result)
            assertEquals("Board not found", failure.reason)

            coVerify { boardRepository.findById("missing") }
            coVerify(inverse = true) { columnRepository.findById(any()) }
            coVerify(inverse = true) { taskRepository.save(any()) }
        }

    @Test
    fun `should fail when column not found`() =
        runTest {
            coEvery { boardRepository.findById("board-1") } returns sampleBoard
            coEvery { columnRepository.findById("missing") } returns null

            val result =
                operation.execute(
                    CreateTaskOperation.Arg(
                        boardId = "board-1",
                        columnId = "missing",
                        title = "Task",
                        description = null,
                        assigneeId = null,
                        dueDate = null,
                    ),
                )

            val failure = assertIs<CreateTaskOperation.Result.Failure>(result)
            assertEquals("Column not found", failure.reason)

            coVerify { columnRepository.findById("missing") }
            coVerify(inverse = true) { taskRepository.save(any()) }
        }

    private fun sampleTask(
        id: String,
        position: Int,
    ): Task =
        Task(
            id = com.kanban.common.TaskId(id),
            boardId = BoardId("board-1"),
            columnId = ColumnId("column-1"),
            title = "Existing",
            description = null,
            assigneeId = null,
            position = position,
            dueDate = null,
            archived = false,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )
}
