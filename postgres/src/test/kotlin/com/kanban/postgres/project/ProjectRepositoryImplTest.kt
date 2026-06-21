package com.kanban.postgres.project

import com.kanban.common.ProjectId
import com.kanban.common.UserId
import com.kanban.project.Project
import com.kanban.project.ProjectRepository
import java.time.Instant
import java.util.UUID
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
internal class ProjectRepositoryImplTest {
    @Autowired
    private lateinit var db: DatabaseClient

    private lateinit var projectGenerator: ProjectGenerator
    private lateinit var projectRepository: ProjectRepository

    @BeforeEach
    fun setUp() {
        projectGenerator = ProjectGenerator(db)
        projectRepository = ProjectRepositoryImpl(db)
    }

    @AfterEach
    fun tearDown() =
        runTest {
            projectGenerator.deleteAll()
        }

    @Test
    fun `should save new project and find by id`() =
        runTest {
            val ownerId = UUID.randomUUID().toString()
            val now = Instant.now()
            val project =
                Project(
                    id = ProjectId("00000000-0000-0000-0000-000000000115"),
                    ownerId = UserId(ownerId),
                    name = "Test Project",
                    description = "Some description",
                    createdAt = now,
                    updatedAt = now,
                )

            val saved = projectRepository.save(project)

            assertEquals("00000000-0000-0000-0000-000000000115", saved.id.value)

            val found = projectRepository.findById("00000000-0000-0000-0000-000000000115")
            assertNotNull(found)
            assertEquals("00000000-0000-0000-0000-000000000115", found.id.value)
            assertEquals(ownerId, found.ownerId.value)
            assertEquals("Test Project", found.name)
            assertEquals("Some description", found.description)
        }

    @Test
    fun `should save project with null description`() =
        runTest {
            val ownerId = UUID.randomUUID().toString()
            val now = Instant.now()
            val project =
                Project(
                    id = ProjectId("00000000-0000-0000-0000-000000000118"),
                    ownerId = UserId(ownerId),
                    name = "No description",
                    description = null,
                    createdAt = now,
                    updatedAt = now,
                )

            projectRepository.save(project)

            val found = projectRepository.findById("00000000-0000-0000-0000-000000000118")
            assertNotNull(found)
            assertNull(found.description)
        }

    @Test
    fun `should return null for unknown id`() =
        runTest {
            val found = projectRepository.findById("00000000-0000-0000-0000-000000000104")
            assertNull(found)
        }

    @Test
    fun `should list projects by owner id ordered by created_at`() =
        runTest {
            val ownerId = UUID.randomUUID().toString()
            val firstId = projectGenerator.createAndInsert(ownerId = ownerId, name = "First")
            val secondId = projectGenerator.createAndInsert(ownerId = ownerId, name = "Second")
            projectGenerator.createAndInsert(name = "Other Owner")

            val list = projectRepository.listByOwnerId(ownerId)

            assertEquals(2, list.size)
            assertEquals(firstId, list[0].id.value)
            assertEquals(secondId, list[1].id.value)
        }

    @Test
    fun `should return empty list when owner has no projects`() =
        runTest {
            val list = projectRepository.listByOwnerId(UUID.randomUUID().toString())
            assertTrue(list.isEmpty())
        }

    @Test
    fun `should update existing project`() =
        runTest {
            val projectId = projectGenerator.createAndInsert()
            val existing = projectRepository.findById(projectId)!!
            val updated =
                existing.copy(
                    name = "Updated Name",
                    description = "New description",
                    updatedAt = Instant.now(),
                )

            val saved = projectRepository.save(updated)

            assertEquals("Updated Name", saved.name)
            val reloaded = projectRepository.findById(projectId)
            assertNotNull(reloaded)
            assertEquals("Updated Name", reloaded.name)
            assertEquals("New description", reloaded.description)
        }

    @Test
    fun `should delete project by id`() =
        runTest {
            val projectId = projectGenerator.createAndInsert()
            assertNotNull(projectRepository.findById(projectId))

            projectRepository.delete(projectId)

            assertNull(projectRepository.findById(projectId))
        }
}
