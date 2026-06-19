package com.kanban.postgres.identity

import com.kanban.identity.RefreshTokenRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.r2dbc.core.DatabaseClient

@DataR2dbcTest
internal class RefreshTokenRepositoryImplTest {
    @Autowired
    private lateinit var db: DatabaseClient

    private lateinit var userGenerator: UserGenerator
    private lateinit var refreshTokenGenerator: RefreshTokenGenerator
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    @BeforeEach
    fun setUp() {
        userGenerator = UserGenerator(db)
        refreshTokenGenerator = RefreshTokenGenerator(db)
        refreshTokenRepository = RefreshTokenRepositoryImpl(db)
    }

    @AfterEach
    fun tearDown() =
        runTest {
            refreshTokenGenerator.deleteAll()
            userGenerator.deleteAll()
        }

    @Test
    fun `should save and find token by hash`() =
        runTest {
            val userId = userGenerator.createAndInsert()
            val tokenHash = "test-hash-${java.util.UUID.randomUUID()}"
            val expiresAt = Instant.now().plus(7, ChronoUnit.DAYS)

            refreshTokenRepository.save(userId, tokenHash, expiresAt)

            val found = refreshTokenRepository.findByTokenHash(tokenHash)
            assertNotNull(found)
            assertTrue(found.first == userId)
        }

    @Test
    fun `should return null for unknown hash`() =
        runTest {
            val found = refreshTokenRepository.findByTokenHash("nonexistent-hash")
            assertNull(found)
        }

    @Test
    fun `should delete tokens by user id`() =
        runTest {
            val userId = userGenerator.createAndInsert()
            val tokenHash = "delete-me-hash"
            val expiresAt = Instant.now().plus(7, ChronoUnit.DAYS)
            refreshTokenRepository.save(userId, tokenHash, expiresAt)

            refreshTokenRepository.deleteByUserId(userId)

            val found = refreshTokenRepository.findByTokenHash(tokenHash)
            assertNull(found)
        }

    @Test
    fun `should not return expired token`() =
        runTest {
            val userId = userGenerator.createAndInsert()
            val tokenHash = "expired-hash"
            val expiresAt = Instant.now().minus(1, ChronoUnit.DAYS)
            refreshTokenRepository.save(userId, tokenHash, expiresAt)

            val found = refreshTokenRepository.findByTokenHash(tokenHash)
            assertNull(found)
        }
}
