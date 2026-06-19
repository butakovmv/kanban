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

class ListProjectsOperationImplTest {
    private val projectRepository = mockk<ProjectRepository>()
    private val operation = ListProjectsOperationImpl(projectRepository)

    private val projects =
        listOf(
            Project(
                id = ProjectId("p-1"),
                ownerId = UserId("user-1"),
                name = "Project 1",
                description = null,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            ),
            Project(
                id = ProjectId("p-2"),
                ownerId = UserId("user-1"),
                name = "Project 2",
                description = "d",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            ),
        )

    @Test
    fun `should return list of projects for owner`() =
        runTest {
            coEvery { projectRepository.listByOwnerId("user-1") } returns projects

            val result = operation.execute(ListProjectsOperation.Arg(ownerId = "user-1"))

            val success = assertIs<ListProjectsOperation.Result.Success>(result)
            assertEquals(2, success.projects.size)
            assertEquals(projects, success.projects)

            coVerify { projectRepository.listByOwnerId("user-1") }
        }

    @Test
    fun `should return empty list when owner has no projects`() =
        runTest {
            coEvery { projectRepository.listByOwnerId("user-2") } returns emptyList()

            val result = operation.execute(ListProjectsOperation.Arg(ownerId = "user-2"))

            val success = assertIs<ListProjectsOperation.Result.Success>(result)
            assertEquals(emptyList(), success.projects)

            coVerify { projectRepository.listByOwnerId("user-2") }
        }
}
