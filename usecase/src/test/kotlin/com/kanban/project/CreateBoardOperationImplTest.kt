package com.kanban.project

import com.kanban.common.ProjectId
import com.kanban.common.UserId
import com.kanban.identity.CheckTariffLimitsOperation
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class CreateBoardOperationImplTest {
    private val projectRepository = mockk<ProjectRepository>()
    private val boardRepository = mockk<BoardRepository>()
    private val columnRepository = mockk<ColumnRepository>()
    private val checkTariffLimitsOperation = mockk<CheckTariffLimitsOperation>()
    private val operation =
        CreateBoardOperationImpl(
            projectRepository,
            boardRepository,
            columnRepository,
            checkTariffLimitsOperation,
        )

    private val sampleProject =
        Project(
            id = ProjectId("project-1"),
            ownerId = UserId("user-1"),
            name = "P",
            description = null,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )

    @Test
    fun `should create board with default columns`() =
        runTest {
            coEvery { projectRepository.findById("project-1") } returns sampleProject
            coEvery { checkTariffLimitsOperation.execute(any()) } returns CheckTariffLimitsOperation.Result.Allowed
            coEvery { boardRepository.listByProjectId("project-1") } returns emptyList()
            coEvery { boardRepository.save(any()) } answers { firstArg() }
            coEvery { columnRepository.save(any()) } answers { firstArg() }

            val result =
                operation.execute(
                    CreateBoardOperation.Arg(projectId = "project-1", name = "Main Board"),
                )

            val success = assertIs<CreateBoardOperation.Result.Success>(result)
            assertEquals("Main Board", success.view.board.name)
            assertEquals(ProjectId("project-1"), success.view.board.projectId)
            assertEquals(0, success.view.board.position)
            assertEquals(3, success.view.columns.size)
            assertEquals(listOf("To Do", "In Progress", "Done"), success.view.columns.map { it.name })
            assertEquals(listOf(0, 1, 2), success.view.columns.map { it.position })

            coVerify { projectRepository.findById("project-1") }
            coVerify { checkTariffLimitsOperation.execute(any()) }
            coVerify { boardRepository.save(any()) }
            coVerify(exactly = 3) { columnRepository.save(any()) }
        }

    @Test
    fun `should fail when project not found`() =
        runTest {
            coEvery { projectRepository.findById("missing") } returns null

            val result =
                operation.execute(
                    CreateBoardOperation.Arg(projectId = "missing", name = "Main Board"),
                )

            val failure = assertIs<CreateBoardOperation.Result.Failure>(result)
            assertEquals("Project not found", failure.reason)

            coVerify { projectRepository.findById("missing") }
            coVerify(inverse = true) { checkTariffLimitsOperation.execute(any()) }
            coVerify(inverse = true) { boardRepository.save(any()) }
        }

    @Test
    fun `should fail when tariff limit exceeded`() =
        runTest {
            coEvery { projectRepository.findById("project-1") } returns sampleProject
            coEvery {
                checkTariffLimitsOperation.execute(any())
            } returns CheckTariffLimitsOperation.Result.Denied("Board limit exceeded: max 1")

            val result =
                operation.execute(
                    CreateBoardOperation.Arg(projectId = "project-1", name = "Main Board"),
                )

            val failure = assertIs<CreateBoardOperation.Result.Failure>(result)
            assertEquals("Board limit exceeded: max 1", failure.reason)

            coVerify { checkTariffLimitsOperation.execute(any()) }
            coVerify(inverse = true) { boardRepository.save(any()) }
            coVerify(inverse = true) { columnRepository.save(any()) }
        }

    @Test
    fun `should compute position from existing boards count`() =
        runTest {
            val existing =
                Board(
                    id = com.kanban.common.BoardId("b-1"),
                    projectId = ProjectId("project-1"),
                    name = "First",
                    position = 0,
                    createdAt = Instant.now(),
                )
            coEvery { projectRepository.findById("project-1") } returns sampleProject
            coEvery { checkTariffLimitsOperation.execute(any()) } returns CheckTariffLimitsOperation.Result.Allowed
            coEvery { boardRepository.listByProjectId("project-1") } returns listOf(existing)
            coEvery { boardRepository.save(any()) } answers { firstArg() }
            coEvery { columnRepository.save(any()) } answers { firstArg() }

            val result =
                operation.execute(
                    CreateBoardOperation.Arg(projectId = "project-1", name = "Second Board"),
                )

            val success = assertIs<CreateBoardOperation.Result.Success>(result)
            assertEquals(1, success.view.board.position)
        }
}
