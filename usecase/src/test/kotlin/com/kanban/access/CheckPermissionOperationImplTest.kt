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

class CheckPermissionOperationImplTest {
    private val groupPermissionRepository = mockk<GroupPermissionRepository>()
    private val operation = CheckPermissionOperationImpl(groupPermissionRepository)

    private val globalReadProject =
        Permission(
            id = PermissionId("perm-1"),
            resource = "project",
            action = "read",
            targetId = null,
            createdAt = Instant.now(),
        )

    private val boardWriteTarget1 =
        Permission(
            id = PermissionId("perm-2"),
            resource = "board",
            action = "write",
            targetId = "board-1",
            createdAt = Instant.now(),
        )

    @Test
    fun `should allow when user has matching global permission`() =
        runTest {
            coEvery {
                groupPermissionRepository.listPermissionsForUser("user-1")
            } returns listOf(globalReadProject)

            val result =
                operation.execute(
                    CheckPermissionOperation.Arg(
                        userId = "user-1",
                        resource = "project",
                        action = "read",
                        targetId = null,
                    ),
                )

            assertIs<CheckPermissionOperation.Result.Allowed>(result)
            coVerify { groupPermissionRepository.listPermissionsForUser("user-1") }
        }

    @Test
    fun `should allow when user has permission matching targetId`() =
        runTest {
            coEvery {
                groupPermissionRepository.listPermissionsForUser("user-1")
            } returns listOf(boardWriteTarget1)

            val result =
                operation.execute(
                    CheckPermissionOperation.Arg(
                        userId = "user-1",
                        resource = "board",
                        action = "write",
                        targetId = "board-1",
                    ),
                )

            assertIs<CheckPermissionOperation.Result.Allowed>(result)
        }

    @Test
    fun `should allow when global permission matches even if specific targetId is requested`() =
        runTest {
            coEvery {
                groupPermissionRepository.listPermissionsForUser("user-1")
            } returns listOf(globalReadProject)

            val result =
                operation.execute(
                    CheckPermissionOperation.Arg(
                        userId = "user-1",
                        resource = "project",
                        action = "read",
                        targetId = "project-42",
                    ),
                )

            assertIs<CheckPermissionOperation.Result.Allowed>(result)
        }

    @Test
    fun `should deny when user has no permissions at all`() =
        runTest {
            coEvery {
                groupPermissionRepository.listPermissionsForUser("user-1")
            } returns emptyList()

            val result =
                operation.execute(
                    CheckPermissionOperation.Arg(
                        userId = "user-1",
                        resource = "project",
                        action = "read",
                        targetId = null,
                    ),
                )

            val denied = assertIs<CheckPermissionOperation.Result.Denied>(result)
            assert(denied.reason.contains("user-1"))
            assert(denied.reason.contains("read"))
            assert(denied.reason.contains("project"))
        }

    @Test
    fun `should deny when resource does not match`() =
        runTest {
            coEvery {
                groupPermissionRepository.listPermissionsForUser("user-1")
            } returns listOf(globalReadProject)

            val result =
                operation.execute(
                    CheckPermissionOperation.Arg(
                        userId = "user-1",
                        resource = "task",
                        action = "read",
                        targetId = null,
                    ),
                )

            val denied = assertIs<CheckPermissionOperation.Result.Denied>(result)
            assert(denied.reason.contains("task"))
        }

    @Test
    fun `should deny when action does not match`() =
        runTest {
            coEvery {
                groupPermissionRepository.listPermissionsForUser("user-1")
            } returns listOf(globalReadProject)

            val result =
                operation.execute(
                    CheckPermissionOperation.Arg(
                        userId = "user-1",
                        resource = "project",
                        action = "write",
                        targetId = null,
                    ),
                )

            assertIs<CheckPermissionOperation.Result.Denied>(result)
        }

    @Test
    fun `should deny when target-specific permission does not match requested targetId`() =
        runTest {
            coEvery {
                groupPermissionRepository.listPermissionsForUser("user-1")
            } returns listOf(boardWriteTarget1)

            val result =
                operation.execute(
                    CheckPermissionOperation.Arg(
                        userId = "user-1",
                        resource = "board",
                        action = "write",
                        targetId = "board-2",
                    ),
                )

            val denied = assertIs<CheckPermissionOperation.Result.Denied>(result)
            assertEquals(
                "User user-1 has no permission 'write' on board (target=board-2)",
                denied.reason,
            )
        }

    @Test
    fun `should deny when target-specific permission requested but user has only global one with different action`() =
        runTest {
            coEvery {
                groupPermissionRepository.listPermissionsForUser("user-1")
            } returns
                listOf(
                    Permission(
                        id = PermissionId("perm-3"),
                        resource = "board",
                        action = "read",
                        targetId = null,
                        createdAt = Instant.now(),
                    ),
                )

            val result =
                operation.execute(
                    CheckPermissionOperation.Arg(
                        userId = "user-1",
                        resource = "board",
                        action = "write",
                        targetId = "board-1",
                    ),
                )

            assertIs<CheckPermissionOperation.Result.Denied>(result)
        }

    @Test
    fun `denied reason should include targetId when it is provided`() =
        runTest {
            coEvery {
                groupPermissionRepository.listPermissionsForUser("user-1")
            } returns emptyList()

            val result =
                operation.execute(
                    CheckPermissionOperation.Arg(
                        userId = "user-1",
                        resource = "project",
                        action = "read",
                        targetId = "project-99",
                    ),
                )

            val denied = assertIs<CheckPermissionOperation.Result.Denied>(result)
            assert(denied.reason.contains("project-99"))
        }
}
