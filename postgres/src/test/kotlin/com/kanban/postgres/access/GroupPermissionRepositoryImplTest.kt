package com.kanban.postgres.access

import com.kanban.access.GroupPermissionRepository
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.r2dbc.core.DatabaseClient

@DataR2dbcTest
internal class GroupPermissionRepositoryImplTest {
    @Autowired
    private lateinit var db: DatabaseClient

    private lateinit var generator: AccessGenerator
    private lateinit var repository: GroupPermissionRepository

    private lateinit var groupId: String
    private lateinit var otherGroupId: String
    private lateinit var permA: String
    private lateinit var permB: String

    @BeforeEach
    fun setUp() =
        runTest {
            generator = AccessGenerator(db)
            repository = GroupPermissionRepositoryImpl(db)
            groupId = generator.insertGroup(name = "Group A")
            otherGroupId = generator.insertGroup(name = "Group B")
            permA = generator.insertPermission(resource = "project", action = "read")
            permB = generator.insertPermission(resource = "board", action = "write")
        }

    @AfterEach
    fun tearDown() =
        runTest {
            generator.deleteAll()
        }

    @Test
    fun `should grant permission and list for group`() =
        runTest {
            generator.grantPermission(groupId, permA)

            val perms = repository.listPermissionsForGroup(groupId)
            assertEquals(1, perms.size)
            assertEquals(permA, perms.first().id.value)
            assertEquals("project", perms.first().resource)
        }

    @Test
    fun `should list multiple permissions for group`() =
        runTest {
            generator.grantPermission(groupId, permA)
            generator.grantPermission(groupId, permB)

            val perms = repository.listPermissionsForGroup(groupId)
            assertEquals(2, perms.size)
            assertTrue(perms.any { it.id.value == permA })
            assertTrue(perms.any { it.id.value == permB })
        }

    @Test
    fun `should return empty list when group has no permissions`() =
        runTest {
            val perms = repository.listPermissionsForGroup(groupId)
            assertTrue(perms.isEmpty())
        }

    @Test
    fun `should not include permissions from other groups`() =
        runTest {
            generator.grantPermission(groupId, permA)

            val perms = repository.listPermissionsForGroup(otherGroupId)
            assertTrue(perms.isEmpty())
        }

    @Test
    fun `should revoke permission from group`() =
        runTest {
            generator.grantPermission(groupId, permA)
            assertTrue(repository.listPermissionsForGroup(groupId).isNotEmpty())

            repository.revoke(groupId, permA)

            assertTrue(repository.listPermissionsForGroup(groupId).isEmpty())
        }

    @Test
    fun `should list permissions for user through group membership`() =
        runTest {
            val userId = UUID.randomUUID().toString()
            generator.addMember(groupId, userId)
            generator.grantPermission(groupId, permA)
            generator.grantPermission(groupId, permB)

            val perms = repository.listPermissionsForUser(userId)
            assertEquals(2, perms.size)
            assertTrue(perms.any { it.id.value == permA })
            assertTrue(perms.any { it.id.value == permB })
        }

    @Test
    fun `should return empty permissions for user not in any group`() =
        runTest {
            val perms = repository.listPermissionsForUser(UUID.randomUUID().toString())
            assertTrue(perms.isEmpty())
        }

    @Test
    fun `should not include permissions from groups user is not a member of`() =
        runTest {
            val userId = UUID.randomUUID().toString()
            generator.grantPermission(groupId, permA)

            val perms = repository.listPermissionsForUser(userId)
            assertTrue(perms.isEmpty())
        }

    @Test
    fun `should delete all permissions by group`() =
        runTest {
            generator.grantPermission(groupId, permA)
            generator.grantPermission(groupId, permB)
            generator.grantPermission(otherGroupId, permA)

            repository.deleteAllByGroup(groupId)

            assertTrue(repository.listPermissionsForGroup(groupId).isEmpty())
            assertEquals(1, repository.listPermissionsForGroup(otherGroupId).size)
        }
}
