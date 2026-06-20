package com.kanban.access

import com.kanban.common.GroupId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DeleteGroupOperationImplTest {
    private val groupRepository = mockk<GroupRepository>()
    private val groupMemberRepository = mockk<GroupMemberRepository>()
    private val groupPermissionRepository = mockk<GroupPermissionRepository>()
    private val operation =
        DeleteGroupOperationImpl(groupRepository, groupMemberRepository, groupPermissionRepository)

    private val sampleGroup =
        Group(
            id = GroupId("group-1"),
            name = "Admins",
            description = null,
            createdAt = Instant.now(),
        )

    @Test
    fun `should cascade delete members, group-permissions and the group itself`() =
        runTest {
            coEvery { groupRepository.findById("group-1") } returns sampleGroup
            coEvery { groupMemberRepository.deleteAllByGroup("group-1") } returns Unit
            coEvery { groupPermissionRepository.deleteAllByGroup("group-1") } returns Unit
            coEvery { groupRepository.delete("group-1") } returns Unit

            val result = operation.execute(DeleteGroupOperation.Arg(groupId = "group-1"))

            assertIs<DeleteGroupOperation.Result.Success>(result)
            coVerify { groupRepository.findById("group-1") }
            coVerify { groupMemberRepository.deleteAllByGroup("group-1") }
            coVerify { groupPermissionRepository.deleteAllByGroup("group-1") }
            coVerify { groupRepository.delete("group-1") }
        }

    @Test
    fun `should return NotFound when group not found`() =
        runTest {
            coEvery { groupRepository.findById("missing") } returns null

            val result = operation.execute(DeleteGroupOperation.Arg(groupId = "missing"))

            assertIs<DeleteGroupOperation.Result.NotFound>(result)
            coVerify { groupRepository.findById("missing") }
            coVerify(inverse = true) { groupMemberRepository.deleteAllByGroup(any()) }
            coVerify(inverse = true) { groupPermissionRepository.deleteAllByGroup(any()) }
            coVerify(inverse = true) { groupRepository.delete(any()) }
        }

    @Test
    fun `should not call any cascade delete when group is missing`() =
        runTest {
            coEvery { groupRepository.findById("missing") } returns null

            operation.execute(DeleteGroupOperation.Arg(groupId = "missing"))

            coVerify(inverse = true) { groupMemberRepository.deleteAllByGroup(any()) }
            coVerify(inverse = true) { groupPermissionRepository.deleteAllByGroup(any()) }
        }
}
