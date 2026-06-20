package com.kanban.search

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SearchOperationImplTest {
    private val searchRepository = mockk<SearchRepository>()
    private val operation = SearchOperationImpl(searchRepository)

    @Test
    fun `should return results and total from repository`() =
        runTest {
            val criteria =
                SearchCriteria(
                    query = "test",
                    projectId = null,
                    boardId = null,
                    status = null,
                    priority = null,
                    assigneeId = null,
                    dueDateFrom = null,
                    dueDateTo = null,
                    limit = 20,
                    offset = 0,
                )
            val now = Instant.now()
            val expectedResults =
                listOf(
                    SearchResult(
                        id = "task-1",
                        title = "Test task",
                        description = "A test",
                        status = "",
                        priority = null,
                        assigneeId = null,
                        boardId = "board-1",
                        columnId = "column-1",
                        projectId = "project-1",
                        dueDate = null,
                        createdAt = now,
                        updatedAt = now,
                        rank = 0.5f,
                    ),
                )
            coEvery { searchRepository.search(criteria) } returns expectedResults
            coEvery { searchRepository.count(criteria) } returns 1L

            val result = operation.execute(SearchOperation.Arg(criteria))

            val success = assertIs<SearchOperation.Result.Success>(result)
            assertEquals(expectedResults, success.results)
            assertEquals(1L, success.total)

            coVerify { searchRepository.search(criteria) }
            coVerify { searchRepository.count(criteria) }
        }

    @Test
    fun `should return empty results when nothing matches`() =
        runTest {
            val criteria =
                SearchCriteria(
                    query = "nonexistent",
                    projectId = null,
                    boardId = null,
                    status = null,
                    priority = null,
                    assigneeId = null,
                    dueDateFrom = null,
                    dueDateTo = null,
                    limit = 20,
                    offset = 0,
                )
            coEvery { searchRepository.search(criteria) } returns emptyList()
            coEvery { searchRepository.count(criteria) } returns 0L

            val result = operation.execute(SearchOperation.Arg(criteria))

            val success = assertIs<SearchOperation.Result.Success>(result)
            assertEquals(emptyList<SearchResult>(), success.results)
            assertEquals(0L, success.total)
        }

    @Test
    fun `should pass pagination parameters to repository`() =
        runTest {
            val criteria =
                SearchCriteria(
                    query = "test",
                    projectId = null,
                    boardId = null,
                    status = null,
                    priority = null,
                    assigneeId = null,
                    dueDateFrom = null,
                    dueDateTo = null,
                    limit = 10,
                    offset = 5,
                )
            coEvery { searchRepository.search(criteria) } returns emptyList()
            coEvery { searchRepository.count(criteria) } returns 20L

            operation.execute(SearchOperation.Arg(criteria))

            coVerify {
                searchRepository.search(
                    withArg { c ->
                        assertEquals(10, c.limit)
                        assertEquals(5, c.offset)
                    },
                )
                searchRepository.count(
                    withArg { c ->
                        assertEquals(10, c.limit)
                        assertEquals(5, c.offset)
                    },
                )
            }
        }
}
