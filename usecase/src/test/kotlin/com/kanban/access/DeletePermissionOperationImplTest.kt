package com.kanban.access

import com.kanban.common.PermissionId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DeletePermissionOperationImplTest {
    private val permissionRepository = mockk<PermissionRepository>()
    private val operation = DeletePermissionOperationImpl(permissionRepository)

    private val samplePermission =
        Permission(
            id = PermissionId("perm-1"),
            resource = "project",
            action = "read",
            targetId = "project-1",
            createdAt = Instant.now(),
        )

    @Test
    fun `should delete existing permission`() =
        runTest {
            coEvery { permissionRepository.findById("perm-1") } returns samplePermission
            coEvery { permissionRepository.delete("perm-1") } returns Unit

            val result = operation.execute(DeletePermissionOperation.Arg(permissionId = "perm-1"))

            assertIs<DeletePermissionOperation.Result.Success>(result)
            coVerify { permissionRepository.findById("perm-1") }
            coVerify { permissionRepository.delete("perm-1") }
        }

    @Test
    fun `should return NotFound when permission not found`() =
        runTest {
            coEvery { permissionRepository.findById("missing") } returns null

            val result = operation.execute(DeletePermissionOperation.Arg(permissionId = "missing"))

            assertIs<DeletePermissionOperation.Result.NotFound>(result)
            coVerify { permissionRepository.findById("missing") }
            coVerify(inverse = true) { permissionRepository.delete(any()) }
        }
}
