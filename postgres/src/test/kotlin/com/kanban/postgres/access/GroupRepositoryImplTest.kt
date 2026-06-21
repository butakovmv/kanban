package com.kanban.postgres.access

import com.kanban.access.Group
import com.kanban.access.GroupRepository
import com.kanban.common.GroupId
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
internal class GroupRepositoryImplTest {
    @Autowired
    private lateinit var db: DatabaseClient

    private lateinit var generator: AccessGenerator
    private lateinit var repository: GroupRepository

    @BeforeEach
    fun setUp() {
        generator = AccessGenerator(db)
        repository = GroupRepositoryImpl(db)
    }

    @AfterEach
    fun tearDown() =
        runTest {
            generator.deleteAll()
        }

    @Test
    fun `should save new group and find by id`() =
        runTest {
            val now = Instant.now()
            val group =
                Group(
                    id = GroupId("00000000-0000-0000-0000-000000000109"),
                    name = "Test Group",
                    description = "A test group",
                    createdAt = now,
                )

            val saved = repository.save(group)

            assertEquals("00000000-0000-0000-0000-000000000109", saved.id.value)
            assertEquals("Test Group", saved.name)

            val found = repository.findById("00000000-0000-0000-0000-000000000109")
            assertNotNull(found)
            assertEquals("00000000-0000-0000-0000-000000000109", found.id.value)
            assertEquals("Test Group", found.name)
            assertEquals("A test group", found.description)
        }

    @Test
    fun `should save group with null description`() =
        runTest {
            val now = Instant.now()
            val group =
                Group(
                    id = GroupId("00000000-0000-0000-0000-000000000118"),
                    name = "No description",
                    description = null,
                    createdAt = now,
                )

            repository.save(group)

            val found = repository.findById("00000000-0000-0000-0000-000000000118")
            assertNotNull(found)
            assertNull(found.description)
        }

    @Test
    fun `should return null for unknown id`() =
        runTest {
            val found = repository.findById("00000000-0000-0000-0000-000000000100")
            assertNull(found)
        }

    @Test
    fun `should list all groups ordered by name`() =
        runTest {
            val firstId = generator.insertGroup(name = "Alpha")
            val secondId = generator.insertGroup(name = "Beta")
            val thirdId = generator.insertGroup(name = "Gamma")

            val list = repository.listAll()

            assertEquals(3, list.size)
            assertEquals(listOf(firstId, secondId, thirdId), list.map { it.id.value })
            assertEquals(listOf("Alpha", "Beta", "Gamma"), list.map { it.name })
        }

    @Test
    fun `should return empty list when no groups exist`() =
        runTest {
            val list = repository.listAll()
            assertTrue(list.isEmpty())
        }

    @Test
    fun `should update existing group`() =
        runTest {
            val groupId = generator.insertGroup(name = "Original", description = "Original desc")
            val existing = repository.findById(groupId)!!
            val updated =
                existing.copy(
                    name = "Updated Name",
                    description = "Updated description",
                )

            repository.save(updated)

            val reloaded = repository.findById(groupId)
            assertNotNull(reloaded)
            assertEquals("Updated Name", reloaded.name)
            assertEquals("Updated description", reloaded.description)
        }

    @Test
    fun `should delete group by id`() =
        runTest {
            val groupId = generator.insertGroup()
            assertNotNull(repository.findById(groupId))

            repository.delete(groupId)

            assertNull(repository.findById(groupId))
        }
}
