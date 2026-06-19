package com.kanban.identity

import com.kanban.common.AccessToken
import com.kanban.common.AuthTokens
import com.kanban.common.Email
import com.kanban.common.PasswordHash
import com.kanban.common.RefreshToken
import com.kanban.common.UserId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class LoginWithPasswordOperationImplTest {
    private val userRepository = mockk<UserRepository>()
    private val passwordHasher = mockk<PasswordHasher>()
    private val tokenProvider = mockk<TokenProvider>()
    private val operation = LoginWithPasswordOperationImpl(userRepository, passwordHasher, tokenProvider)

    private val sampleUser =
        User(
            id = UserId("user-1"),
            email = Email("user@kanban.test"),
            passwordHash = PasswordHash("hashed-password"),
            displayName = "User",
            totpSecret = null,
            totpEnabled = false,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )

    @Test
    fun `should login with valid credentials`() =
        runTest {
            coEvery { userRepository.findByEmail("user@kanban.test") } returns sampleUser
            every { passwordHasher.verify("correct-password", "hashed-password") } returns true
            coEvery { tokenProvider.generateTokens("user-1") } returns
                AuthTokens(
                    AccessToken("access-token"),
                    RefreshToken("refresh-token"),
                )

            val result =
                operation.execute(
                    LoginWithPasswordOperation.Arg(email = "user@kanban.test", password = "correct-password"),
                )

            val success = assertIs<LoginWithPasswordOperation.Result.Success>(result)
            assertEquals("access-token", success.tokens.accessToken.value)
            assertEquals("refresh-token", success.tokens.refreshToken.value)
            assertEquals("user-1", success.user.id.value)
            assertEquals("User", success.user.displayName)

            coVerify { userRepository.findByEmail("user@kanban.test") }
            coVerify { passwordHasher.verify("correct-password", "hashed-password") }
            coVerify { tokenProvider.generateTokens("user-1") }
        }

    @Test
    fun `should fail with wrong password`() =
        runTest {
            coEvery { userRepository.findByEmail("user@kanban.test") } returns sampleUser
            every { passwordHasher.verify("wrong-password", "hashed-password") } returns false

            val result =
                operation.execute(
                    LoginWithPasswordOperation.Arg(email = "user@kanban.test", password = "wrong-password"),
                )

            val failure = assertIs<LoginWithPasswordOperation.Result.Failure>(result)
            assertEquals("Invalid email or password", failure.reason)

            coVerify { userRepository.findByEmail("user@kanban.test") }
            coVerify { passwordHasher.verify("wrong-password", "hashed-password") }
            coVerify(inverse = true) { tokenProvider.generateTokens(any()) }
        }

    @Test
    fun `should fail for unknown email`() =
        runTest {
            coEvery { userRepository.findByEmail("unknown@kanban.test") } returns null

            val result =
                operation.execute(
                    LoginWithPasswordOperation.Arg(email = "unknown@kanban.test", password = "pwd"),
                )

            val failure = assertIs<LoginWithPasswordOperation.Result.Failure>(result)
            assertEquals("Invalid email or password", failure.reason)

            coVerify { userRepository.findByEmail("unknown@kanban.test") }
            coVerify(inverse = true) { passwordHasher.verify(any(), any()) }
            coVerify(inverse = true) { tokenProvider.generateTokens(any()) }
        }
}
