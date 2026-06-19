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

class GetProjectOperationImplTest {
    private val projectRepository = mockk<ProjectRepository>()
    private val operation = GetProjectOperationImpl(projectRepository)

    private val sampleProject =
        Project(
            id = ProjectId("project-1"),
            ownerId = UserId("user-1"),
            name = "My Project",
            description = "Desc",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )

    @Test
    fun `should return project when found`() =
        runTest {
            coEvery { projectRepository.findById("project-1") } returns sampleProject

            val result = operation.execute(GetProjectOperation.Arg(projectId = "project-1"))

            val success = assertIs<GetProjectOperation.Result.Success>(result)
            assertEquals(sampleProject, success.project)

            coVerify { projectRepository.findById("project-1") }
        }

    @Test
    fun `should return NotFound when project not found`() =
        runTest {
            coEvery { projectRepository.findById("missing") } returns null

            val result = operation.execute(GetProjectOperation.Arg(projectId = "missing"))

            assertIs<GetProjectOperation.Result.NotFound>(result)
            coVerify { projectRepository.findById("missing") }
        }
}
