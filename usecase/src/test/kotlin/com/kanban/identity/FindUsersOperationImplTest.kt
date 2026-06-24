package com.kanban.identity

import com.kanban.common.Email
import com.kanban.common.PasswordHash
import com.kanban.common.UserId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class FindUsersOperationImplTest {
    private val userRepository = mockk<UserRepository>()
    private val operation = FindUsersOperationImpl(userRepository)

    private val sampleUser =
        User(
            id = UserId("user-1"),
            email = Email("user@kanban.test"),
            passwordHash = PasswordHash("hash"),
            displayName = "Alice",
            totpSecret = null,
            totpEnabled = false,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )

    private val sampleUser2 =
        User(
            id = UserId("user-2"),
            email = Email("user2@kanban.test"),
            passwordHash = PasswordHash("hash"),
            displayName = "Bob",
            totpSecret = null,
            totpEnabled = false,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )

    @Test
    fun `should return display info for found users`() =
        runTest {
            coEvery { userRepository.findByIds(listOf("user-1", "user-2")) } returns listOf(sampleUser, sampleUser2)

            val result = operation.execute(FindUsersOperation.Arg(userIds = listOf("user-1", "user-2")))

            assertEquals(2, result.users.size)
            assertEquals("user-1", result.users[0].id)
            assertEquals("Alice", result.users[0].displayName)
            assertEquals("user-2", result.users[1].id)
            assertEquals("Bob", result.users[1].displayName)
            coVerify { userRepository.findByIds(listOf("user-1", "user-2")) }
        }

    @Test
    fun `should return empty list for empty input`() =
        runTest {
            coEvery { userRepository.findByIds(emptyList()) } returns emptyList()

            val result = operation.execute(FindUsersOperation.Arg(userIds = emptyList()))

            assertEquals(0, result.users.size)
        }

    @Test
    fun `should skip non-existent users`() =
        runTest {
            coEvery { userRepository.findByIds(listOf("user-1", "missing")) } returns listOf(sampleUser)

            val result = operation.execute(FindUsersOperation.Arg(userIds = listOf("user-1", "missing")))

            assertEquals(1, result.users.size)
            assertEquals("user-1", result.users[0].id)
            assertEquals("Alice", result.users[0].displayName)
        }
}
