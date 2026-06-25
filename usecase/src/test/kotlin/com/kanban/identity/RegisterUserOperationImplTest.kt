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
    private val userTariffRepository = mockk<UserTariffRepository>()
    private val tariffRepository = mockk<TariffRepository>()
    private val operation =
        RegisterUserOperationImpl(
            userRepository = userRepository,
            passwordHasher = passwordHasher,
            tokenProvider = tokenProvider,
            userTariffRepository = userTariffRepository,
            tariffRepository = tariffRepository,
        )

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
            coEvery { tariffRepository.findByName("Free") } returns
                Tariff(
                    id = com.kanban.common.TariffId("t-free"),
                    name = "Free",
                    limits = TariffLimits(5, 3, 50, 10, 100),
                    createdAt = java.time.Instant.now(),
                )
            coEvery { userTariffRepository.save(any()) } answers { firstArg() }
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

            coVerify { passwordHasher.hash(password) }
            coVerify { userRepository.existsByEmail(email) }
            coVerify { userRepository.save(any()) }
            coVerify { tariffRepository.findByName("Free") }
            coVerify { userTariffRepository.save(any()) }
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

            val failure = assertIs<RegisterUserOperation.Result.Failure>(result)
            assertEquals("Email already registered", failure.reason)

            coVerify { userRepository.existsByEmail("existing@kanban.test") }
            coVerify(inverse = true) { passwordHasher.hash(any()) }
            coVerify(inverse = true) { userRepository.save(any()) }
        }

    @Test
    fun `should fail on invalid email`() =
        runTest {
            val result =
                operation.execute(
                    RegisterUserOperation.Arg(email = "bad", password = "pwd", displayName = "n"),
                )

            val failure = assertIs<RegisterUserOperation.Result.Failure>(result)
            assertEquals("Invalid email: bad", failure.reason)

            coVerify(inverse = true) { userRepository.save(any()) }
            coVerify(inverse = true) { tokenProvider.generateTokens(any()) }
        }
}
