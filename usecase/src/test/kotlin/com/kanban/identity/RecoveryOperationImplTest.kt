package com.kanban.identity

import com.kanban.common.Email
import com.kanban.common.PasswordHash
import com.kanban.common.UserId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RecoveryOperationImplTest {
    private val userRepository = mockk<UserRepository>()
    private val recoveryTokenRepository = mockk<RecoveryTokenRepository>()
    private val passwordHasher = mockk<PasswordHasher>()
    private val emailService = mockk<EmailService>()
    private val operation =
        RecoveryOperationImpl(
            userRepository = userRepository,
            recoveryTokenRepository = recoveryTokenRepository,
            passwordHasher = passwordHasher,
            emailService = emailService,
        )

    private val sampleUser =
        User(
            id = UserId("user-1"),
            email = Email("user@kanban.test"),
            passwordHash = PasswordHash("old-hash"),
            displayName = "User",
            totpSecret = null,
            totpEnabled = false,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )

    @Test
    fun `should send recovery token for existing email`() =
        runTest {
            coEvery { userRepository.findByEmail("user@kanban.test") } returns sampleUser
            coEvery { recoveryTokenRepository.save(any(), any(), any()) } returns Unit
            coEvery { emailService.sendRecoveryToken(any(), any()) } returns Unit

            val result =
                operation.execute(
                    RecoveryOperation.Arg(
                        email = "user@kanban.test",
                        action = RecoveryOperation.Action.REQUEST,
                    ),
                )

            val success = assertIs<RecoveryOperation.Result.Success>(result)
            assertEquals("Recovery token sent", success.message)

            coVerify { userRepository.findByEmail("user@kanban.test") }
            coVerify { recoveryTokenRepository.save(any(), any(), any()) }
            coVerify { emailService.sendRecoveryToken("user@kanban.test", any()) }
        }

    @Test
    fun `should not reveal if email does not exist`() =
        runTest {
            coEvery { userRepository.findByEmail("unknown@kanban.test") } returns null

            val result =
                operation.execute(
                    RecoveryOperation.Arg(
                        email = "unknown@kanban.test",
                        action = RecoveryOperation.Action.REQUEST,
                    ),
                )

            val success = assertIs<RecoveryOperation.Result.Success>(result)
            assertEquals("If the email exists, a recovery token has been sent", success.message)

            coVerify { userRepository.findByEmail("unknown@kanban.test") }
            coVerify(inverse = true) { recoveryTokenRepository.save(any(), any(), any()) }
            coVerify(inverse = true) { emailService.sendRecoveryToken(any(), any()) }
        }

    @Test
    fun `should reset password with valid token`() =
        runTest {
            val rawToken = "valid-recovery-token"
            val tokenHash = sha256(rawToken)
            val newPassword = "new-secure-password"
            val newPasswordHash = "hashed-new-password"

            coEvery { recoveryTokenRepository.findByTokenHash(tokenHash) } returns
                Pair("user-1", Instant.now().plusSeconds(3600))
            coEvery { userRepository.findById("user-1") } returns sampleUser
            coEvery { passwordHasher.hash(newPassword) } returns newPasswordHash
            coEvery { userRepository.save(any()) } answers { firstArg() }
            coEvery { recoveryTokenRepository.deleteByTokenHash(tokenHash) } returns Unit

            val result =
                operation.execute(
                    RecoveryOperation.Arg(
                        email = "user@kanban.test",
                        token = rawToken,
                        newPassword = newPassword,
                        action = RecoveryOperation.Action.RESET,
                    ),
                )

            val success = assertIs<RecoveryOperation.Result.Success>(result)
            assertEquals("Password reset successfully", success.message)

            coVerify { recoveryTokenRepository.findByTokenHash(tokenHash) }
            coVerify { userRepository.findById("user-1") }
            coVerify { passwordHasher.hash(newPassword) }
            coVerify { userRepository.save(any()) }
            coVerify { recoveryTokenRepository.deleteByTokenHash(tokenHash) }
        }

    @Test
    fun `should fail with invalid token`() =
        runTest {
            val rawToken = "invalid-token"
            val tokenHash = sha256(rawToken)

            coEvery { recoveryTokenRepository.findByTokenHash(tokenHash) } returns null

            val result =
                operation.execute(
                    RecoveryOperation.Arg(
                        email = "user@kanban.test",
                        token = rawToken,
                        newPassword = "new-pwd",
                        action = RecoveryOperation.Action.RESET,
                    ),
                )

            val failure = assertIs<RecoveryOperation.Result.Failure>(result)
            assertEquals("Invalid or expired token", failure.reason)

            coVerify { recoveryTokenRepository.findByTokenHash(tokenHash) }
            coVerify(inverse = true) { userRepository.save(any()) }
        }

    @Test
    fun `should fail with empty token`() =
        runTest {
            val result =
                operation.execute(
                    RecoveryOperation.Arg(
                        email = "user@kanban.test",
                        token = "",
                        newPassword = "new-pwd",
                        action = RecoveryOperation.Action.RESET,
                    ),
                )

            val failure = assertIs<RecoveryOperation.Result.Failure>(result)
            assertEquals("Token is required", failure.reason)

            coVerify(inverse = true) { recoveryTokenRepository.findByTokenHash(any()) }
        }

    @Test
    fun `should fail with empty new password`() =
        runTest {
            val result =
                operation.execute(
                    RecoveryOperation.Arg(
                        email = "user@kanban.test",
                        token = "some-token",
                        newPassword = "",
                        action = RecoveryOperation.Action.RESET,
                    ),
                )

            val failure = assertIs<RecoveryOperation.Result.Failure>(result)
            assertEquals("New password is required", failure.reason)
        }

    @Test
    fun `should fail when user not found after token validation`() =
        runTest {
            val rawToken = "orphan-token"
            val tokenHash = sha256(rawToken)

            coEvery { recoveryTokenRepository.findByTokenHash(tokenHash) } returns
                Pair("deleted-user-id", Instant.now().plusSeconds(3600))
            coEvery { userRepository.findById("deleted-user-id") } returns null

            val result =
                operation.execute(
                    RecoveryOperation.Arg(
                        email = "user@kanban.test",
                        token = rawToken,
                        newPassword = "new-pwd",
                        action = RecoveryOperation.Action.RESET,
                    ),
                )

            val failure = assertIs<RecoveryOperation.Result.Failure>(result)
            assertEquals("User not found", failure.reason)

            coVerify(inverse = true) { userRepository.save(any()) }
            coVerify(inverse = true) { recoveryTokenRepository.deleteByTokenHash(any()) }
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
