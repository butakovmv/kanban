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

class FindPermissionsOperationImplTest {
    private val permissionRepository = mockk<PermissionRepository>()
    private val operation = FindPermissionsOperationImpl(permissionRepository)

    @Test
    fun `should return permissions matching resource and targetId`() =
        runTest {
            val permissions =
                listOf(
                    Permission(
                        id = PermissionId("perm-1"),
                        resource = "project",
                        action = "read",
                        targetId = "project-1",
                        createdAt = Instant.now(),
                    ),
                )
            coEvery { permissionRepository.findByResource("project", "project-1") } returns permissions

            val result =
                operation.execute(
                    FindPermissionsOperation.Arg(
                        resource = "project",
                        targetId = "project-1",
                    ),
                )

            val success = assertIs<FindPermissionsOperation.Result.Success>(result)
            assertEquals(permissions, success.permissions)

            coVerify { permissionRepository.findByResource("project", "project-1") }
        }

    @Test
    fun `should return empty list when no permissions match`() =
        runTest {
            coEvery { permissionRepository.findByResource("board", null) } returns emptyList()

            val result =
                operation.execute(
                    FindPermissionsOperation.Arg(
                        resource = "board",
                        targetId = null,
                    ),
                )

            val success = assertIs<FindPermissionsOperation.Result.Success>(result)
            assertEquals(emptyList(), success.permissions)
        }
}
