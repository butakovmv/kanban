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

class ReorderColumnsOperationImplTest {
    private val boardRepository = mockk<BoardRepository>()
    private val columnRepository = mockk<ColumnRepository>()
    private val operation = ReorderColumnsOperationImpl(boardRepository, columnRepository)

    private val sampleBoard =
        Board(
            id = BoardId("board-1"),
            projectId = ProjectId("project-1"),
            name = "B",
            position = 0,
            createdAt = Instant.now(),
        )

    private fun column(
        id: String,
        name: String,
        position: Int,
    ): Column =
        Column(
            id = ColumnId(id),
            boardId = BoardId("board-1"),
            name = name,
            position = position,
            wipLimit = null,
            createdAt = Instant.now(),
        )

    @Test
    fun `should reorder columns according to new order`() =
        runTest {
            val c1 = column("c-1", "To Do", 0)
            val c2 = column("c-2", "In Progress", 1)
            val c3 = column("c-3", "Done", 2)
            val current = listOf(c1, c2, c3)

            coEvery { boardRepository.findById("board-1") } returns sampleBoard
            coEvery { columnRepository.listByBoardId("board-1") } returns current
            coEvery { columnRepository.updatePositions(any()) } returns Unit

            val newOrder = listOf("c-3", "c-1", "c-2")
            val result = operation.execute(ReorderColumnsOperation.Arg("board-1", newOrder))

            val success = assertIs<ReorderColumnsOperation.Result.Success>(result)
            assertEquals(listOf("c-3", "c-1", "c-2"), success.columns.map { it.id.value })
            assertEquals(listOf(0, 1, 2), success.columns.map { it.position })
            assertEquals(listOf("Done", "To Do", "In Progress"), success.columns.map { it.name })

            coVerify { columnRepository.updatePositions(any()) }
        }

    @Test
    fun `should return BoardNotFound when board not found`() =
        runTest {
            coEvery { boardRepository.findById("missing") } returns null

            val result = operation.execute(ReorderColumnsOperation.Arg("missing", listOf("c-1")))

            assertIs<ReorderColumnsOperation.Result.BoardNotFound>(result)
            coVerify { boardRepository.findById("missing") }
            coVerify(inverse = true) { columnRepository.listByBoardId(any()) }
            coVerify(inverse = true) { columnRepository.updatePositions(any()) }
        }

    @Test
    fun `should return InvalidColumns when column ids do not match`() =
        runTest {
            val c1 = column("c-1", "To Do", 0)
            val c2 = column("c-2", "In Progress", 1)

            coEvery { boardRepository.findById("board-1") } returns sampleBoard
            coEvery { columnRepository.listByBoardId("board-1") } returns listOf(c1, c2)

            val result = operation.execute(ReorderColumnsOperation.Arg("board-1", listOf("c-1", "c-3")))

            assertIs<ReorderColumnsOperation.Result.InvalidColumns>(result)
            coVerify(inverse = true) { columnRepository.updatePositions(any()) }
        }

    @Test
    fun `should return InvalidColumns when count differs`() =
        runTest {
            val c1 = column("c-1", "To Do", 0)
            val c2 = column("c-2", "In Progress", 1)

            coEvery { boardRepository.findById("board-1") } returns sampleBoard
            coEvery { columnRepository.listByBoardId("board-1") } returns listOf(c1, c2)

            val result = operation.execute(ReorderColumnsOperation.Arg("board-1", listOf("c-1")))

            assertIs<ReorderColumnsOperation.Result.InvalidColumns>(result)
            coVerify(inverse = true) { columnRepository.updatePositions(any()) }
        }
}
