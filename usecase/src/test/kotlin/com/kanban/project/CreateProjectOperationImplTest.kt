package com.kanban.project

import com.kanban.identity.CheckTariffLimitsOperation
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class CreateProjectOperationImplTest {
    private val projectRepository = mockk<ProjectRepository>()
    private val boardRepository = mockk<BoardRepository>()
    private val columnRepository = mockk<ColumnRepository>()
    private val checkTariffLimitsOperation = mockk<CheckTariffLimitsOperation>()
    private val operation =
        CreateProjectOperationImpl(
            projectRepository,
            boardRepository,
            columnRepository,
            checkTariffLimitsOperation,
        )

    @Test
    fun `should create project with default board`() =
        runTest {
            coEvery {
                checkTariffLimitsOperation.execute(any())
            } returns CheckTariffLimitsOperation.Result.Allowed
            coEvery { projectRepository.save(any()) } answers { firstArg() }
            coEvery { boardRepository.save(any()) } answers { firstArg() }
            coEvery { columnRepository.save(any()) } answers { firstArg() }

            val result =
                operation.execute(
                    CreateProjectOperation.Arg(
                        ownerId = "user-1",
                        name = "My Project",
                        description = "Some description",
                    ),
                )

            val success = assertIs<CreateProjectOperation.Result.Success>(result)
            assertEquals("user-1", success.project.ownerId.value)
            assertEquals("My Project", success.project.name)
            assertEquals("Some description", success.project.description)
            assertEquals(success.project.createdAt, success.project.updatedAt)

            coVerify {
                checkTariffLimitsOperation.execute(
                    CheckTariffLimitsOperation.Arg(
                        userId = "user-1",
                        resourceType = CheckTariffLimitsOperation.ResourceType.PROJECT,
                        requestedCount = 1,
                    ),
                )
            }
            coVerify { projectRepository.save(any()) }
            coVerify { boardRepository.save(any()) }
            coVerify(exactly = 4) { columnRepository.save(any()) }
        }

    @Test
    fun `should fail when tariff limit exceeded`() =
        runTest {
            coEvery {
                checkTariffLimitsOperation.execute(any())
            } returns CheckTariffLimitsOperation.Result.Denied("Project limit exceeded: max 2")

            val result =
                operation.execute(
                    CreateProjectOperation.Arg(
                        ownerId = "user-1",
                        name = "My Project",
                        description = null,
                    ),
                )

            val failure = assertIs<CreateProjectOperation.Result.Failure>(result)
            assertEquals("Project limit exceeded: max 2", failure.reason)

            coVerify { checkTariffLimitsOperation.execute(any()) }
            coVerify(inverse = true) { projectRepository.save(any()) }
            coVerify(inverse = true) { boardRepository.save(any()) }
            coVerify(inverse = true) { columnRepository.save(any()) }
        }
}
