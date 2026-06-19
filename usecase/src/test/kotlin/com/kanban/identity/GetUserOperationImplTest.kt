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

class GetUserOperationImplTest {
    private val userRepository = mockk<UserRepository>()
    private val operation = GetUserOperationImpl(userRepository)

    private val sampleUser =
        User(
            id = UserId("user-1"),
            email = Email("user@kanban.test"),
            passwordHash = PasswordHash("hash"),
            displayName = "User",
            totpSecret = null,
            totpEnabled = false,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )

    @Test
    fun `should return user when found`() =
        runTest {
            coEvery { userRepository.findById("user-1") } returns sampleUser

            val result = operation.execute(GetUserOperation.Arg(userId = "user-1"))

            val success = assertIs<GetUserOperation.Result.Success>(result)
            assertEquals(sampleUser, success.user)
            coVerify { userRepository.findById("user-1") }
        }

    @Test
    fun `should return NotFound when user not found`() =
        runTest {
            coEvery { userRepository.findById("missing") } returns null

            val result = operation.execute(GetUserOperation.Arg(userId = "missing"))

            assertIs<GetUserOperation.Result.NotFound>(result)
        }
}
