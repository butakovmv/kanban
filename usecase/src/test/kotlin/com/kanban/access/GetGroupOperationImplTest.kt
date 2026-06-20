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

class GetGroupOperationImplTest {
    private val groupRepository = mockk<GroupRepository>()
    private val operation = GetGroupOperationImpl(groupRepository)

    private val sampleGroup =
        Group(
            id = GroupId("group-1"),
            name = "Admins",
            description = null,
            createdAt = Instant.now(),
        )

    @Test
    fun `should return group when found`() =
        runTest {
            coEvery { groupRepository.findById("group-1") } returns sampleGroup

            val result = operation.execute(GetGroupOperation.Arg(groupId = "group-1"))

            val success = assertIs<GetGroupOperation.Result.Success>(result)
            assertEquals(sampleGroup, success.group)

            coVerify { groupRepository.findById("group-1") }
        }

    @Test
    fun `should return NotFound when group not found`() =
        runTest {
            coEvery { groupRepository.findById("missing") } returns null

            val result = operation.execute(GetGroupOperation.Arg(groupId = "missing"))

            assertIs<GetGroupOperation.Result.NotFound>(result)
            coVerify { groupRepository.findById("missing") }
        }
}
