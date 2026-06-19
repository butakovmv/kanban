package com.kanban.identity

import com.kanban.common.AccessToken
import com.kanban.common.AuthTokens
import com.kanban.common.RefreshToken
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RegisterUserOperationImplTest {
    private val userRepository = mockk<UserRepository>()
    private val passwordHasher = mockk<PasswordHasher>()
    private val tokenProvider = mockk<TokenProvider>()
    private val operation = RegisterUserOperationImpl(userRepository, passwordHasher, tokenProvider)

    @Test
    fun `should register user and return tokens`() =
        runTest {
            val email = "new@kanban.test"
            val password = "secure-password"
            val name = "New User"
            val hashedPassword = "hashed:$password"

            every { passwordHasher.hash(password) } returns hashedPassword
            coEvery { userRepository.existsByEmail(email) } returns false
            coEvery { userRepository.save(any()) } answers {
                firstArg()
            }
            coEvery { tokenProvider.generateTokens(any()) } returns
                AuthTokens(
                    AccessToken("access-token"),
                    RefreshToken("refresh-token"),
                )

            val result =
                operation.execute(
                    RegisterUserOperation.Arg(email = email, password = password, displayName = name),
                )

            val success = assertIs<RegisterUserOperation.Result.Success>(result)
            assertEquals(name, success.user.displayName)
            assertEquals("access-token", success.tokens.accessToken.value)
            assertEquals("refresh-token", success.tokens.refreshToken.value)

            coVerify { userRepository.save(any()) }
            coVerify { tokenProvider.generateTokens(any()) }
        }

    @Test
    fun `should fail on duplicate email`() =
        runTest {
            coEvery { userRepository.existsByEmail("existing@kanban.test") } returns true

            val result =
                operation.execute(
                    RegisterUserOperation.Arg(email = "existing@kanban.test", password = "pwd", displayName = "n"),
                )

            assertIs<RegisterUserOperation.Result.Failure>(result)
        }

    @Test
    fun `should fail on invalid email`() =
        runTest {
            val result =
                operation.execute(
                    RegisterUserOperation.Arg(email = "bad", password = "pwd", displayName = "n"),
                )

            assertIs<RegisterUserOperation.Result.Failure>(result)
        }
}
