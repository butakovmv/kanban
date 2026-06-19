package com.kanban.project

import com.kanban.common.ProjectId
import com.kanban.common.UserId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class UpdateProjectOperationImplTest {
    private val projectRepository = mockk<ProjectRepository>()
    private val operation = UpdateProjectOperationImpl(projectRepository)

    private val sampleProject =
        Project(
            id = ProjectId("project-1"),
            ownerId = UserId("user-1"),
            name = "Old Name",
            description = "Old Desc",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )

    @Test
    fun `should update name and description`() =
        runTest {
            coEvery { projectRepository.findById("project-1") } returns sampleProject
            coEvery { projectRepository.save(any()) } answers { firstArg() }

            val result =
                operation.execute(
                    UpdateProjectOperation.Arg(
                        projectId = "project-1",
                        name = "New Name",
                        description = "New Desc",
                    ),
                )

            val success = assertIs<UpdateProjectOperation.Result.Success>(result)
            assertEquals("New Name", success.project.name)
            assertEquals("New Desc", success.project.description)

            coVerify { projectRepository.findById("project-1") }
            coVerify { projectRepository.save(any()) }
        }

    @Test
    fun `should update only name when description is null`() =
        runTest {
            coEvery { projectRepository.findById("project-1") } returns sampleProject
            coEvery { projectRepository.save(any()) } answers { firstArg() }

            val result =
                operation.execute(
                    UpdateProjectOperation.Arg(
                        projectId = "project-1",
                        name = "New Name",
                        description = null,
                    ),
                )

            val success = assertIs<UpdateProjectOperation.Result.Success>(result)
            assertEquals("New Name", success.project.name)
            assertEquals("Old Desc", success.project.description)

            coVerify { projectRepository.save(any()) }
        }

    @Test
    fun `should return NotFound when project not found`() =
        runTest {
            coEvery { projectRepository.findById("missing") } returns null

            val result =
                operation.execute(
                    UpdateProjectOperation.Arg(
                        projectId = "missing",
                        name = "x",
                        description = null,
                    ),
                )

            assertIs<UpdateProjectOperation.Result.NotFound>(result)
            coVerify { projectRepository.findById("missing") }
            coVerify(inverse = true) { projectRepository.save(any()) }
        }
}
