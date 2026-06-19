package com.kanban.project

import com.kanban.common.BoardId
import com.kanban.common.ColumnId
import com.kanban.common.ProjectId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GetBoardOperationImplTest {
    private val boardRepository = mockk<BoardRepository>()
    private val columnRepository = mockk<ColumnRepository>()
    private val operation = GetBoardOperationImpl(boardRepository, columnRepository)

    private val sampleBoard =
        Board(
            id = BoardId("board-1"),
            projectId = ProjectId("project-1"),
            name = "Main Board",
            position = 0,
            createdAt = Instant.now(),
        )

    private val sampleColumns =
        listOf(
            Column(
                id = ColumnId("c-1"),
                boardId = BoardId("board-1"),
                name = "To Do",
                position = 0,
                wipLimit = null,
                createdAt = Instant.now(),
            ),
            Column(
                id = ColumnId("c-2"),
                boardId = BoardId("board-1"),
                name = "In Progress",
                position = 1,
                wipLimit = 5,
                createdAt = Instant.now(),
            ),
            Column(
                id = ColumnId("c-3"),
                boardId = BoardId("board-1"),
                name = "Done",
                position = 2,
                wipLimit = null,
                createdAt = Instant.now(),
            ),
        )

    @Test
    fun `should return board with columns when found`() =
        runTest {
            coEvery { boardRepository.findById("board-1") } returns sampleBoard
            coEvery { columnRepository.listByBoardId("board-1") } returns sampleColumns

            val result = operation.execute(GetBoardOperation.Arg(boardId = "board-1"))

            val success = assertIs<GetBoardOperation.Result.Success>(result)
            assertEquals(sampleBoard, success.view.board)
            assertEquals(sampleColumns, success.view.columns)
            assertEquals(3, success.view.columns.size)

            coVerify { boardRepository.findById("board-1") }
            coVerify { columnRepository.listByBoardId("board-1") }
        }

    @Test
    fun `should return NotFound when board not found`() =
        runTest {
            coEvery { boardRepository.findById("missing") } returns null

            val result = operation.execute(GetBoardOperation.Arg(boardId = "missing"))

            assertIs<GetBoardOperation.Result.NotFound>(result)
            coVerify { boardRepository.findById("missing") }
            coVerify(inverse = true) { columnRepository.listByBoardId(any()) }
        }

    @Test
    fun `should return board with empty columns when board has no columns`() =
        runTest {
            coEvery { boardRepository.findById("board-2") } returns sampleBoard.copy(id = BoardId("board-2"))
            coEvery { columnRepository.listByBoardId("board-2") } returns emptyList()

            val result = operation.execute(GetBoardOperation.Arg(boardId = "board-2"))

            val success = assertIs<GetBoardOperation.Result.Success>(result)
            assertEquals(emptyList(), success.view.columns)
        }
}
