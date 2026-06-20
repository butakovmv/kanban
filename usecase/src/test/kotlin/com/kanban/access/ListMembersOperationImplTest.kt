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

class ListMembersOperationImplTest {
    private val groupMemberRepository = mockk<GroupMemberRepository>()
    private val operation = ListMembersOperationImpl(groupMemberRepository)

    @Test
    fun `should return members for group`() =
        runTest {
            val members =
                listOf(
                    GroupMember(
                        groupId = GroupId("group-1"),
                        userId = "user-1",
                        addedAt = Instant.now(),
                    ),
                    GroupMember(
                        groupId = GroupId("group-1"),
                        userId = "user-2",
                        addedAt = Instant.now(),
                    ),
                )
            coEvery { groupMemberRepository.listMembers("group-1") } returns members

            val result = operation.execute(ListMembersOperation.Arg(groupId = "group-1"))

            val success = assertIs<ListMembersOperation.Result.Success>(result)
            assertEquals(members, success.members)
            coVerify { groupMemberRepository.listMembers("group-1") }
        }

    @Test
    fun `should return empty list when group has no members`() =
        runTest {
            coEvery { groupMemberRepository.listMembers("empty") } returns emptyList()

            val result = operation.execute(ListMembersOperation.Arg(groupId = "empty"))

            val success = assertIs<ListMembersOperation.Result.Success>(result)
            assertEquals(emptyList(), success.members)
        }
}
