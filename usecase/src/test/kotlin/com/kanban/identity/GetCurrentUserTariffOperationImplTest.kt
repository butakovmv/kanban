package com.kanban.identity

import com.kanban.common.TariffId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GetCurrentUserTariffOperationImplTest {
    private val userTariffRepository = mockk<UserTariffRepository>()
    private val tariffRepository = mockk<TariffRepository>()
    private val operation = GetCurrentUserTariffOperationImpl(userTariffRepository, tariffRepository)

    private val freeTariff =
        Tariff(
            id = TariffId("t-1"),
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
    fun `should return tariff when user has active tariff`() =
        runTest {
            coEvery { userTariffRepository.findActiveByUserId("user-1") } returns activeUserTariff
            coEvery { tariffRepository.findById("t-1") } returns freeTariff

            val result = operation.execute(GetCurrentUserTariffOperation.Arg(userId = "user-1"))

            val success = assertIs<GetCurrentUserTariffOperation.Result.Success>(result)
            assertNotNull(success.tariff)
            assertIs<TariffInfo>(success.tariff)

            coVerify { userTariffRepository.findActiveByUserId("user-1") }
            coVerify { tariffRepository.findById("t-1") }
        }

    @Test
    fun `should return not found when no active tariff`() =
        runTest {
            coEvery { userTariffRepository.findActiveByUserId("user-1") } returns null

            val result = operation.execute(GetCurrentUserTariffOperation.Arg(userId = "user-1"))

            assertIs<GetCurrentUserTariffOperation.Result.NotFound>(result)

            coVerify { userTariffRepository.findActiveByUserId("user-1") }
            coVerify(inverse = true) { tariffRepository.findById(any()) }
        }

    @Test
    fun `should return not found when tariff not in repository`() =
        runTest {
            coEvery { userTariffRepository.findActiveByUserId("user-1") } returns activeUserTariff
            coEvery { tariffRepository.findById("t-1") } returns null

            val result = operation.execute(GetCurrentUserTariffOperation.Arg(userId = "user-1"))

            assertIs<GetCurrentUserTariffOperation.Result.NotFound>(result)

            coVerify { userTariffRepository.findActiveByUserId("user-1") }
            coVerify { tariffRepository.findById("t-1") }
        }

    @Test
    fun `should return correct tariff info`() =
        runTest {
            coEvery { userTariffRepository.findActiveByUserId("user-1") } returns activeUserTariff
            coEvery { tariffRepository.findById("t-1") } returns freeTariff

            val result = operation.execute(GetCurrentUserTariffOperation.Arg(userId = "user-1"))

            val success = assertIs<GetCurrentUserTariffOperation.Result.Success>(result)
            val info = success.tariff
            assert(info.name == "Free")
            assert(info.maxProjects == 2)
            assert(info.maxBoardsPerProject == 3)
            assert(info.maxTasksPerBoard == 10)
            assert(info.maxFileSizeMb == 5)
            assert(info.maxStorageMb == 100)
        }
}
