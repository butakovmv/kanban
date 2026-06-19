package com.kanban.identity

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class LogoutOperationImplTest {
    private val refreshTokenRepository = mockk<RefreshTokenRepository>()
    private val operation = LogoutOperationImpl(refreshTokenRepository)

    @Test
    fun `should delete tokens on logout`() =
        runTest {
            val refreshToken = "token-to-revoke"
            val tokenHash = sha256(refreshToken)
            val userId = "user-1"
            coEvery { refreshTokenRepository.findByTokenHash(tokenHash) } returns
                Pair(userId, Instant.now().plusSeconds(3600))
            coEvery { refreshTokenRepository.deleteByUserId(userId) } returns Unit

            val result = operation.execute(LogoutOperation.Arg(refreshToken))

            assertIs<LogoutOperation.Result.Success>(result)

            coVerify { refreshTokenRepository.findByTokenHash(tokenHash) }
            coVerify { refreshTokenRepository.deleteByUserId(userId) }
        }

    @Test
    fun `should succeed idempotently for already revoked token`() =
        runTest {
            val refreshToken = "already-revoked"
            val tokenHash = sha256(refreshToken)
            coEvery { refreshTokenRepository.findByTokenHash(tokenHash) } returns null

            val result = operation.execute(LogoutOperation.Arg(refreshToken))

            assertIs<LogoutOperation.Result.Success>(result)

            coVerify { refreshTokenRepository.findByTokenHash(tokenHash) }
            coVerify(inverse = true) { refreshTokenRepository.deleteByUserId(any()) }
        }

    /**
     * Вычисляет SHA-256 хеш (дублирует логику реализации для теста).
     */
    private fun sha256(input: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
