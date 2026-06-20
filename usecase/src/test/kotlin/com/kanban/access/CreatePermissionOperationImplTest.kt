package com.kanban.access

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class CreatePermissionOperationImplTest {
    private val permissionRepository = mockk<PermissionRepository>()
    private val operation = CreatePermissionOperationImpl(permissionRepository)

    @Test
    fun `should create permission with trimmed resource and action`() =
        runTest {
            coEvery { permissionRepository.save(any()) } answers { firstArg() }

            val result =
                operation.execute(
                    CreatePermissionOperation.Arg(
                        resource = "  project  ",
                        action = "  read  ",
                        targetId = "project-1",
                    ),
                )

            val success = assertIs<CreatePermissionOperation.Result.Success>(result)
            assertEquals("project", success.permission.resource)
            assertEquals("read", success.permission.action)
            assertEquals("project-1", success.permission.targetId)
            val permissionId = success.permission.id.value
            assert(permissionId.isNotBlank())

            coVerify { permissionRepository.save(any()) }
        }

    @Test
    fun `should create global permission when targetId is null`() =
        runTest {
            coEvery { permissionRepository.save(any()) } answers { firstArg() }

            val result =
                operation.execute(
                    CreatePermissionOperation.Arg(
                        resource = "board",
                        action = "admin",
                        targetId = null,
                    ),
                )

            val success = assertIs<CreatePermissionOperation.Result.Success>(result)
            assertEquals("board", success.permission.resource)
            assertEquals("admin", success.permission.action)
            assertEquals(null, success.permission.targetId)
        }

    @Test
    fun `should fail when resource is blank`() =
        runTest {
            val result =
                operation.execute(
                    CreatePermissionOperation.Arg(
                        resource = "   ",
                        action = "read",
                        targetId = null,
                    ),
                )

            val failure = assertIs<CreatePermissionOperation.Result.Failure>(result)
            assertEquals("Resource must not be blank", failure.reason)
            coVerify(inverse = true) { permissionRepository.save(any()) }
        }

    @Test
    fun `should fail when action is blank`() =
        runTest {
            val result =
                operation.execute(
                    CreatePermissionOperation.Arg(
                        resource = "project",
                        action = "   ",
                        targetId = null,
                    ),
                )

            val failure = assertIs<CreatePermissionOperation.Result.Failure>(result)
            assertEquals("Action must not be blank", failure.reason)
            coVerify(inverse = true) { permissionRepository.save(any()) }
        }

    @Test
    fun `should fail when both resource and action are blank - resource reported first`() =
        runTest {
            val result =
                operation.execute(
                    CreatePermissionOperation.Arg(
                        resource = "   ",
                        action = "  ",
                        targetId = null,
                    ),
                )

            val failure = assertIs<CreatePermissionOperation.Result.Failure>(result)
            assertEquals("Resource must not be blank", failure.reason)
        }
}
