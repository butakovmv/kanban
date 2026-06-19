package com.kanban.postgres.identity

import com.kanban.identity.UserTariff
import com.kanban.identity.UserTariffRepository
import java.time.Instant
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.r2dbc.core.DatabaseClient

@DataR2dbcTest
internal class UserTariffRepositoryImplTest {
    @Autowired
    private lateinit var db: DatabaseClient

    private lateinit var userGenerator: UserGenerator
    private lateinit var tariffGenerator: TariffGenerator
    private lateinit var userTariffGenerator: UserTariffGenerator
    private lateinit var userTariffRepository: UserTariffRepository

    @BeforeEach
    fun setUp() {
        userGenerator = UserGenerator(db)
        tariffGenerator = TariffGenerator(db)
        userTariffGenerator = UserTariffGenerator(db)
        userTariffRepository = UserTariffRepositoryImpl(db)
    }

    @AfterEach
    fun tearDown() =
        runTest {
            userTariffGenerator.deleteAll()
            tariffGenerator.deleteAll()
            userGenerator.deleteAll()
        }

    @Test
    fun `should find active tariff by user id`() =
        runTest {
            val userId = userGenerator.createAndInsert()
            val tariffId = tariffGenerator.createAndInsert()
            userTariffGenerator.createAndInsert(userId = userId, tariffId = tariffId)

            val active = userTariffRepository.findActiveByUserId(userId)

            assertNotNull(active)
            assertEquals(userId, active.userId)
            assertEquals(tariffId, active.tariffId)
        }

    @Test
    fun `should return null for user without tariff`() =
        runTest {
            val userId = userGenerator.createAndInsert()

            val active = userTariffRepository.findActiveByUserId(userId)

            assertNull(active)
        }

    @Test
    fun `should save new user tariff`() =
        runTest {
            val userId = userGenerator.createAndInsert()
            val tariffId = tariffGenerator.createAndInsert()
            val userTariff =
                UserTariff(
                    id = "custom-ut-id",
                    userId = userId,
                    tariffId = tariffId,
                    startsAt = Instant.now().minusSeconds(86400),
                    expiresAt = null,
                    createdAt = Instant.now(),
                )

            val saved = userTariffRepository.save(userTariff)

            assertEquals("custom-ut-id", saved.id)

            val active = userTariffRepository.findActiveByUserId(userId)
            assertNotNull(active)
            assertEquals("custom-ut-id", active.id)
            assertEquals(tariffId, active.tariffId)
        }

    @Test
    fun `should return null for expired tariff`() =
        runTest {
            val userId = userGenerator.createAndInsert()
            val tariffId = tariffGenerator.createAndInsert()
            userTariffGenerator.createAndInsert(
                userId = userId,
                tariffId = tariffId,
                expiresAt = LocalDateTime.now().minusDays(1),
            )

            val active = userTariffRepository.findActiveByUserId(userId)
            assertNull(active)
        }
}
