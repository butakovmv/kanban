package com.kanban.access

import com.kanban.common.GroupId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class AddMemberOperationImplTest {
    private val groupRepository = mockk<GroupRepository>()
    private val groupMemberRepository = mockk<GroupMemberRepository>()
    private val operation = AddMemberOperationImpl(groupRepository, groupMemberRepository)

    private val sampleGroup =
        Group(
            id = GroupId("group-1"),
            name = "Admins",
            description = null,
            createdAt = Instant.now(),
        )

    @Test
    fun `should add member when group exists and user is not yet a member`() =
        runTest {
            coEvery { groupRepository.findById("group-1") } returns sampleGroup
            coEvery { groupMemberRepository.isMember("group-1", "user-1") } returns false
            coEvery { groupMemberRepository.addMember("group-1", "user-1") } returns Unit

            val result = operation.execute(AddMemberOperation.Arg(groupId = "group-1", userId = "user-1"))

            assertIs<AddMemberOperation.Result.Success>(result)
            coVerify { groupRepository.findById("group-1") }
            coVerify { groupMemberRepository.isMember("group-1", "user-1") }
            coVerify { groupMemberRepository.addMember("group-1", "user-1") }
        }

    @Test
    fun `should fail when group not found`() =
        runTest {
            coEvery { groupRepository.findById("missing") } returns null

            val result = operation.execute(AddMemberOperation.Arg(groupId = "missing", userId = "user-1"))

            val failure = assertIs<AddMemberOperation.Result.Failure>(result)
            assertEquals("Group not found", failure.reason)
            coVerify { groupRepository.findById("missing") }
            coVerify(inverse = true) { groupMemberRepository.isMember(any(), any()) }
            coVerify(inverse = true) { groupMemberRepository.addMember(any(), any()) }
        }

    @Test
    fun `should fail when user is already a member`() =
        runTest {
            coEvery { groupRepository.findById("group-1") } returns sampleGroup
            coEvery { groupMemberRepository.isMember("group-1", "user-1") } returns true

            val result = operation.execute(AddMemberOperation.Arg(groupId = "group-1", userId = "user-1"))

            val failure = assertIs<AddMemberOperation.Result.Failure>(result)
            assertEquals("User is already a member of the group", failure.reason)
            coVerify { groupRepository.findById("group-1") }
            coVerify { groupMemberRepository.isMember("group-1", "user-1") }
            coVerify(inverse = true) { groupMemberRepository.addMember(any(), any()) }
        }
}
