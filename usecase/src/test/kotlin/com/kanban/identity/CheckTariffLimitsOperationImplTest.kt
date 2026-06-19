package com.kanban.identity

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class CheckTariffLimitsOperationImplTest {
    private val userTariffRepository = mockk<UserTariffRepository>()
    private val tariffRepository = mockk<TariffRepository>()
    private val operation = CheckTariffLimitsOperationImpl(userTariffRepository, tariffRepository)

    private val freeTariff =
        Tariff(
            id = com.kanban.common.TariffId("t-1"),
            name = "Free",
            limits =
                TariffLimits(
                    maxProjects = 2,
                    maxBoardsPerProject = 3,
                    maxTasksPerBoard = 10,
                    maxFileSizeMb = 5,
                    maxStorageMb = 100,
                ),
            createdAt = Instant.now(),
        )

    private val activeUserTariff =
        UserTariff(
            id = "ut-1",
            userId = "user-1",
            tariffId = "t-1",
            startsAt = Instant.now().minusSeconds(86400),
            expiresAt = null,
            createdAt = Instant.now(),
        )

    @Test
    fun `should allow when within limits`() =
        runTest {
            coEvery { userTariffRepository.findActiveByUserId("user-1") } returns activeUserTariff
            coEvery { tariffRepository.findById("t-1") } returns freeTariff

            val result =
                operation.execute(
                    CheckTariffLimitsOperation.Arg(
                        userId = "user-1",
                        resourceType = CheckTariffLimitsOperation.ResourceType.PROJECT,
                        requestedCount = 1,
                    ),
                )

            assertIs<CheckTariffLimitsOperation.Result.Allowed>(result)

            coVerify { userTariffRepository.findActiveByUserId("user-1") }
            coVerify { tariffRepository.findById("t-1") }
        }

    @Test
    fun `should deny when limit exceeded`() =
        runTest {
            coEvery { userTariffRepository.findActiveByUserId("user-1") } returns activeUserTariff
            coEvery { tariffRepository.findById("t-1") } returns freeTariff

            val result =
                operation.execute(
                    CheckTariffLimitsOperation.Arg(
                        userId = "user-1",
                        resourceType = CheckTariffLimitsOperation.ResourceType.PROJECT,
                        requestedCount = 5,
                    ),
                )

            val denied = assertIs<CheckTariffLimitsOperation.Result.Denied>(result)
            assertEquals("Project limit exceeded: max 2", denied.reason)

            coVerify { userTariffRepository.findActiveByUserId("user-1") }
            coVerify { tariffRepository.findById("t-1") }
        }

    @Test
    fun `should allow at exact boundary`() =
        runTest {
            coEvery { userTariffRepository.findActiveByUserId("user-1") } returns activeUserTariff
            coEvery { tariffRepository.findById("t-1") } returns freeTariff

            val result =
                operation.execute(
                    CheckTariffLimitsOperation.Arg(
                        userId = "user-1",
                        resourceType = CheckTariffLimitsOperation.ResourceType.PROJECT,
                        requestedCount = 2,
                    ),
                )

            assertIs<CheckTariffLimitsOperation.Result.Allowed>(result)
        }

    @Test
    fun `should deny when no active tariff`() =
        runTest {
            coEvery { userTariffRepository.findActiveByUserId("user-1") } returns null

            val result =
                operation.execute(
                    CheckTariffLimitsOperation.Arg(
                        userId = "user-1",
                        resourceType = CheckTariffLimitsOperation.ResourceType.PROJECT,
                        requestedCount = 1,
                    ),
                )

            val denied = assertIs<CheckTariffLimitsOperation.Result.Denied>(result)
            assertEquals("No active tariff", denied.reason)

            coVerify { userTariffRepository.findActiveByUserId("user-1") }
            coVerify(inverse = true) { tariffRepository.findById(any()) }
        }

    @Test
    fun `should deny when tariff not found`() =
        runTest {
            coEvery { userTariffRepository.findActiveByUserId("user-1") } returns activeUserTariff
            coEvery { tariffRepository.findById("t-1") } returns null

            val result =
                operation.execute(
                    CheckTariffLimitsOperation.Arg(
                        userId = "user-1",
                        resourceType = CheckTariffLimitsOperation.ResourceType.PROJECT,
                        requestedCount = 1,
                    ),
                )

            val denied = assertIs<CheckTariffLimitsOperation.Result.Denied>(result)
            assertEquals("Tariff not found", denied.reason)

            coVerify { userTariffRepository.findActiveByUserId("user-1") }
            coVerify { tariffRepository.findById("t-1") }
        }

    @Test
    fun `should allow board when within limit`() =
        runTest {
            coEvery { userTariffRepository.findActiveByUserId("user-1") } returns activeUserTariff
            coEvery { tariffRepository.findById("t-1") } returns freeTariff

            val result =
                operation.execute(
                    CheckTariffLimitsOperation.Arg(
                        userId = "user-1",
                        resourceType = CheckTariffLimitsOperation.ResourceType.BOARD,
                        requestedCount = 2,
                    ),
                )

            assertIs<CheckTariffLimitsOperation.Result.Allowed>(result)
        }

    @Test
    fun `should deny board when limit exceeded`() =
        runTest {
            coEvery { userTariffRepository.findActiveByUserId("user-1") } returns activeUserTariff
            coEvery { tariffRepository.findById("t-1") } returns freeTariff

            val result =
                operation.execute(
                    CheckTariffLimitsOperation.Arg(
                        userId = "user-1",
                        resourceType = CheckTariffLimitsOperation.ResourceType.BOARD,
                        requestedCount = 5,
                    ),
                )

            val denied = assertIs<CheckTariffLimitsOperation.Result.Denied>(result)
            assertEquals("Board limit exceeded: max 3", denied.reason)
        }

    @Test
    fun `should allow task when within limit`() =
        runTest {
            coEvery { userTariffRepository.findActiveByUserId("user-1") } returns activeUserTariff
            coEvery { tariffRepository.findById("t-1") } returns freeTariff

            val result =
                operation.execute(
                    CheckTariffLimitsOperation.Arg(
                        userId = "user-1",
                        resourceType = CheckTariffLimitsOperation.ResourceType.TASK,
                        requestedCount = 5,
                    ),
                )

            assertIs<CheckTariffLimitsOperation.Result.Allowed>(result)
        }

    @Test
    fun `should deny task when limit exceeded`() =
        runTest {
            coEvery { userTariffRepository.findActiveByUserId("user-1") } returns activeUserTariff
            coEvery { tariffRepository.findById("t-1") } returns freeTariff

            val result =
                operation.execute(
                    CheckTariffLimitsOperation.Arg(
                        userId = "user-1",
                        resourceType = CheckTariffLimitsOperation.ResourceType.TASK,
                        requestedCount = 15,
                    ),
                )

            val denied = assertIs<CheckTariffLimitsOperation.Result.Denied>(result)
            assertEquals("Task limit exceeded: max 10", denied.reason)
        }

    @Test
    fun `should allow requestedCount zero`() =
        runTest {
            coEvery { userTariffRepository.findActiveByUserId("user-1") } returns activeUserTariff
            coEvery { tariffRepository.findById("t-1") } returns freeTariff

            val result =
                operation.execute(
                    CheckTariffLimitsOperation.Arg(
                        userId = "user-1",
                        resourceType = CheckTariffLimitsOperation.ResourceType.PROJECT,
                        requestedCount = 0,
                    ),
                )

            assertIs<CheckTariffLimitsOperation.Result.Allowed>(result)
        }
}
