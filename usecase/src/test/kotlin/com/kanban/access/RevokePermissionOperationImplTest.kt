package com.kanban.access

import com.kanban.common.GroupId
import com.kanban.common.PermissionId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RevokePermissionOperationImplTest {
    private val groupRepository = mockk<GroupRepository>()
    private val permissionRepository = mockk<PermissionRepository>()
    private val groupPermissionRepository = mockk<GroupPermissionRepository>()
    private val operation =
        RevokePermissionOperationImpl(groupRepository, permissionRepository, groupPermissionRepository)

    private val sampleGroup =
        Group(
            id = GroupId("group-1"),
            name = "Admins",
            description = null,
            createdAt = Instant.now(),
        )

    private val samplePermission =
        Permission(
            id = PermissionId("perm-1"),
            resource = "project",
            action = "read",
            targetId = null,
            createdAt = Instant.now(),
        )

    @Test
    fun `should revoke permission when group and permission exist`() =
        runTest {
            coEvery { groupRepository.findById("group-1") } returns sampleGroup
            coEvery { permissionRepository.findById("perm-1") } returns samplePermission
            coEvery { groupPermissionRepository.revoke("group-1", "perm-1") } returns Unit

            val result =
                operation.execute(
                    RevokePermissionOperation.Arg(groupId = "group-1", permissionId = "perm-1"),
                )

            assertIs<RevokePermissionOperation.Result.Success>(result)
            coVerify { groupRepository.findById("group-1") }
            coVerify { permissionRepository.findById("perm-1") }
            coVerify { groupPermissionRepository.revoke("group-1", "perm-1") }
        }

    @Test
    fun `should fail when group not found`() =
        runTest {
            coEvery { groupRepository.findById("missing") } returns null

            val result =
                operation.execute(
                    RevokePermissionOperation.Arg(groupId = "missing", permissionId = "perm-1"),
                )

            val failure = assertIs<RevokePermissionOperation.Result.Failure>(result)
            assertEquals("Group not found", failure.reason)
            coVerify { groupRepository.findById("missing") }
            coVerify(inverse = true) { permissionRepository.findById(any()) }
            coVerify(inverse = true) { groupPermissionRepository.revoke(any(), any()) }
        }

    @Test
    fun `should fail when permission not found`() =
        runTest {
            coEvery { groupRepository.findById("group-1") } returns sampleGroup
            coEvery { permissionRepository.findById("missing") } returns null

            val result =
                operation.execute(
                    RevokePermissionOperation.Arg(groupId = "group-1", permissionId = "missing"),
                )

            val failure = assertIs<RevokePermissionOperation.Result.Failure>(result)
            assertEquals("Permission not found", failure.reason)
            coVerify { groupRepository.findById("group-1") }
            coVerify { permissionRepository.findById("missing") }
            coVerify(inverse = true) { groupPermissionRepository.revoke(any(), any()) }
        }
}
