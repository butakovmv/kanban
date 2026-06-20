package com.kanban.access

import com.kanban.common.PermissionId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ListGroupPermissionsOperationImplTest {
    private val groupPermissionRepository = mockk<GroupPermissionRepository>()
    private val operation = ListGroupPermissionsOperationImpl(groupPermissionRepository)

    @Test
    fun `should return permissions for group`() =
        runTest {
            val permissions =
                listOf(
                    Permission(
                        id = PermissionId("perm-1"),
                        resource = "project",
                        action = "read",
                        targetId = null,
                        createdAt = Instant.now(),
                    ),
                    Permission(
                        id = PermissionId("perm-2"),
                        resource = "board",
                        action = "write",
                        targetId = "board-1",
                        createdAt = Instant.now(),
                    ),
                )
            coEvery { groupPermissionRepository.listPermissionsForGroup("group-1") } returns permissions

            val result = operation.execute(ListGroupPermissionsOperation.Arg(groupId = "group-1"))

            val success = assertIs<ListGroupPermissionsOperation.Result.Success>(result)
            assertEquals(permissions, success.permissions)
            coVerify { groupPermissionRepository.listPermissionsForGroup("group-1") }
        }

    @Test
    fun `should return empty list when group has no permissions`() =
        runTest {
            coEvery { groupPermissionRepository.listPermissionsForGroup("empty") } returns emptyList()

            val result = operation.execute(ListGroupPermissionsOperation.Arg(groupId = "empty"))

            val success = assertIs<ListGroupPermissionsOperation.Result.Success>(result)
            assertEquals(emptyList(), success.permissions)
        }
}
