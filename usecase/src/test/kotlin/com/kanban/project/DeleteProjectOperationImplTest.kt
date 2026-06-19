package com.kanban.project

import com.kanban.common.ProjectId
import com.kanban.common.UserId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DeleteProjectOperationImplTest {
    private val projectRepository = mockk<ProjectRepository>()
    private val operation = DeleteProjectOperationImpl(projectRepository)

    private val sampleProject =
        Project(
            id = ProjectId("project-1"),
            ownerId = UserId("user-1"),
            name = "Project",
            description = null,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )

    @Test
    fun `should delete existing project`() =
        runTest {
            coEvery { projectRepository.findById("project-1") } returns sampleProject
            coEvery { projectRepository.delete("project-1") } returns Unit

            val result = operation.execute(DeleteProjectOperation.Arg(projectId = "project-1"))

            assertIs<DeleteProjectOperation.Result.Success>(result)
            coVerify { projectRepository.findById("project-1") }
            coVerify { projectRepository.delete("project-1") }
        }

    @Test
    fun `should return NotFound when project not found`() =
        runTest {
            coEvery { projectRepository.findById("missing") } returns null

            val result = operation.execute(DeleteProjectOperation.Arg(projectId = "missing"))

            assertIs<DeleteProjectOperation.Result.NotFound>(result)
            coVerify { projectRepository.findById("missing") }
            coVerify(inverse = true) { projectRepository.delete(any()) }
        }
}
