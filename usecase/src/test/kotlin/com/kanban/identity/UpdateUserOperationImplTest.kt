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

class UpdateUserOperationImplTest {
    private val userRepository = mockk<UserRepository>()
    private val operation = UpdateUserOperationImpl(userRepository)

    private val sampleUser =
        User(
            id = UserId("user-1"),
            email = Email("old@kanban.test"),
            passwordHash = PasswordHash("hash"),
            displayName = "Old Name",
            totpSecret = null,
            totpEnabled = false,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )

    @Test
    fun `should update display name`() =
        runTest {
            coEvery { userRepository.findById("user-1") } returns sampleUser
            coEvery { userRepository.save(any()) } answers { firstArg() }

            val result =
                operation.execute(
                    UpdateUserOperation.Arg(userId = "user-1", displayName = "New Name", email = null),
                )

            val success = assertIs<UpdateUserOperation.Result.Success>(result)
            assertEquals("New Name", success.user.displayName)
            assertEquals("old@kanban.test", success.user.email.value)

            coVerify { userRepository.findById("user-1") }
            coVerify { userRepository.save(any()) }
        }

    @Test
    fun `should update email`() =
        runTest {
            coEvery { userRepository.findById("user-1") } returns sampleUser
            coEvery { userRepository.save(any()) } answers { firstArg() }

            val result =
                operation.execute(
                    UpdateUserOperation.Arg(userId = "user-1", displayName = null, email = "new@kanban.test"),
                )

            val success = assertIs<UpdateUserOperation.Result.Success>(result)
            assertEquals("new@kanban.test", success.user.email.value)

            coVerify { userRepository.findById("user-1") }
            coVerify { userRepository.save(any()) }
        }

    @Test
    fun `should return NotFound for missing user`() =
        runTest {
            coEvery { userRepository.findById("missing") } returns null

            val result =
                operation.execute(
                    UpdateUserOperation.Arg(userId = "missing", displayName = null, email = null),
                )

            assertIs<UpdateUserOperation.Result.NotFound>(result)

            coVerify { userRepository.findById("missing") }
            coVerify(inverse = true) { userRepository.save(any()) }
        }

    @Test
    fun `should fail on invalid email`() =
        runTest {
            coEvery { userRepository.findById("user-1") } returns sampleUser

            val result =
                operation.execute(
                    UpdateUserOperation.Arg(userId = "user-1", displayName = null, email = "bad"),
                )

            val failure = assertIs<UpdateUserOperation.Result.Failure>(result)
            assertEquals("Invalid email: bad", failure.reason)

            coVerify { userRepository.findById("user-1") }
            coVerify(inverse = true) { userRepository.save(any()) }
        }
}
