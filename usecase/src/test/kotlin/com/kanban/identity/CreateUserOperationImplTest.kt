package com.kanban.identity

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class CreateUserOperationImplTest {
    private val userRepository = mockk<UserRepository>()
    private val operation = CreateUserOperationImpl(userRepository)

    @Test
    fun `should create user successfully`() =
        runTest {
            val email = "test@kanban.test"
            val pwd = "hashed-password"
            val name = "Test User"

            coEvery { userRepository.existsByEmail(email) } returns false
            coEvery { userRepository.save(any()) } answers {
                firstArg()
            }

            val result =
                operation.execute(
                    CreateUserOperation.Arg(email = email, passwordHash = pwd, displayName = name),
                )

            val success = assertIs<CreateUserOperation.Result.Success>(result)
            assertEquals(email, success.user.email.value)
            assertEquals(pwd, success.user.passwordHash.value)
            assertEquals(name, success.user.displayName)

            coVerify { userRepository.existsByEmail(email) }
            coVerify { userRepository.save(any()) }
        }

    @Test
    fun `should fail when email already registered`() =
        runTest {
            val email = "existing@kanban.test"

            coEvery { userRepository.existsByEmail(email) } returns true

            val result =
                operation.execute(
                    CreateUserOperation.Arg(email = email, passwordHash = "pwd", displayName = "n"),
                )

            val failure = assertIs<CreateUserOperation.Result.Failure>(result)
            assertEquals("Email already registered", failure.reason)
        }

    @Test
    fun `should fail on invalid email`() =
        runTest {
            val result =
                operation.execute(
                    CreateUserOperation.Arg(email = "not-an-email", passwordHash = "pwd", displayName = "n"),
                )

            assertIs<CreateUserOperation.Result.Failure>(result)
        }
}
