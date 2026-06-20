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

class ListUserGroupsOperationImplTest {
    private val groupMemberRepository = mockk<GroupMemberRepository>()
    private val operation = ListUserGroupsOperationImpl(groupMemberRepository)

    @Test
    fun `should return groups for user`() =
        runTest {
            val groups =
                listOf(
                    Group(
                        id = GroupId("group-1"),
                        name = "Admins",
                        description = null,
                        createdAt = Instant.now(),
                    ),
                    Group(
                        id = GroupId("group-2"),
                        name = "Editors",
                        description = "Editors team",
                        createdAt = Instant.now(),
                    ),
                )
            coEvery { groupMemberRepository.listGroupsForUser("user-1") } returns groups

            val result = operation.execute(ListUserGroupsOperation.Arg(userId = "user-1"))

            val success = assertIs<ListUserGroupsOperation.Result.Success>(result)
            assertEquals(groups, success.groups)
            coVerify { groupMemberRepository.listGroupsForUser("user-1") }
        }

    @Test
    fun `should return empty list when user is not in any group`() =
        runTest {
            coEvery { groupMemberRepository.listGroupsForUser("user-2") } returns emptyList()

            val result = operation.execute(ListUserGroupsOperation.Arg(userId = "user-2"))

            val success = assertIs<ListUserGroupsOperation.Result.Success>(result)
            assertEquals(emptyList(), success.groups)
        }
}
