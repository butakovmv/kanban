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

class ListGroupsOperationImplTest {
    private val groupRepository = mockk<GroupRepository>()
    private val operation = ListGroupsOperationImpl(groupRepository)

    @Test
    fun `should return all groups`() =
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
            coEvery { groupRepository.listAll() } returns groups

            val result = operation.execute(ListGroupsOperation.Arg)

            val success = assertIs<ListGroupsOperation.Result.Success>(result)
            assertEquals(groups, success.groups)

            coVerify { groupRepository.listAll() }
        }

    @Test
    fun `should return empty list when no groups exist`() =
        runTest {
            coEvery { groupRepository.listAll() } returns emptyList()

            val result = operation.execute(ListGroupsOperation.Arg)

            val success = assertIs<ListGroupsOperation.Result.Success>(result)
            assertEquals(emptyList(), success.groups)

            coVerify { groupRepository.listAll() }
        }
}
