package com.kanban.postgres.access

import com.kanban.access.Permission
import com.kanban.access.PermissionRepository
import com.kanban.common.PermissionId
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.r2dbc.core.DatabaseClient

@DataR2dbcTest
internal class PermissionRepositoryImplTest {
    @Autowired
    private lateinit var db: DatabaseClient

    private lateinit var generator: AccessGenerator
    private lateinit var repository: PermissionRepository

    @BeforeEach
    fun setUp() {
        generator = AccessGenerator(db)
        repository = PermissionRepositoryImpl(db)
    }

    @AfterEach
    fun tearDown() =
        runTest {
            generator.deleteAll()
        }

    @Test
    fun `should save new permission and find by id`() =
        runTest {
            val now = Instant.now()
            val permission =
                Permission(
                    id = PermissionId("00000000-0000-0000-0000-000000000114"),
                    resource = "project",
                    action = "read",
                    targetId = "00000000-0000-0000-0000-000000000046",
                    createdAt = now,
                )

            val saved = repository.save(permission)

            assertEquals("00000000-0000-0000-0000-000000000114", saved.id.value)

            val found = repository.findById("00000000-0000-0000-0000-000000000114")
            assertNotNull(found)
            assertEquals("00000000-0000-0000-0000-000000000114", found.id.value)
            assertEquals("project", found.resource)
            assertEquals("read", found.action)
            assertEquals("00000000-0000-0000-0000-000000000046", found.targetId)
        }

    @Test
    fun `should save permission with null targetId`() =
        runTest {
            val now = Instant.now()
            val permission =
                Permission(
                    id = PermissionId("00000000-0000-0000-0000-000000000119"),
                    resource = "project",
                    action = "admin",
                    targetId = null,
                    createdAt = now,
                )

            repository.save(permission)

            val found = repository.findById("00000000-0000-0000-0000-000000000119")
            assertNotNull(found)
            assertNull(found.targetId)
        }

    @Test
    fun `should return null for unknown id`() =
        runTest {
            val found = repository.findById("00000000-0000-0000-0000-000000000101")
            assertNull(found)
        }

    @Test
    fun `should find permissions by resource`() =
        runTest {
            generator.insertPermission(resource = "project", action = "read")
            generator.insertPermission(resource = "project", action = "write")
            generator.insertPermission(resource = "board", action = "read")

            val projectPerms = repository.findByResource("project", null)

            assertEquals(2, projectPerms.size)
            assertTrue(projectPerms.all { it.resource == "project" })
        }

    @Test
    fun `should find permissions by resource and targetId`() =
        runTest {
            generator.insertPermission(
                resource = "project",
                action = "read",
                targetId = "00000000-0000-0000-0000-000000000120",
            )
            generator.insertPermission(
                resource = "project",
                action = "read",
                targetId = "00000000-0000-0000-0000-000000000052",
            )
            generator.insertPermission(resource = "project", action = "read", targetId = null)

            val t1Perms = repository.findByResource("project", "00000000-0000-0000-0000-000000000120")

            assertEquals(1, t1Perms.size)
            assertEquals("00000000-0000-0000-0000-000000000120", t1Perms.first().targetId)
        }

    @Test
    fun `should return empty list when no permissions match`() =
        runTest {
            val perms = repository.findByResource("nonexistent", null)
            assertTrue(perms.isEmpty())
        }

    @Test
    fun `should update existing permission`() =
        runTest {
            val permId = generator.insertPermission(resource = "project", action = "read")
            val existing = repository.findById(permId)!!
            val updated = existing.copy(action = "admin")

            repository.save(updated)

            val reloaded = repository.findById(permId)
            assertNotNull(reloaded)
            assertEquals("admin", reloaded.action)
        }

    @Test
    fun `should delete permission by id`() =
        runTest {
            val permId = generator.insertPermission()
            assertNotNull(repository.findById(permId))

            repository.delete(permId)

            assertNull(repository.findById(permId))
        }
}
