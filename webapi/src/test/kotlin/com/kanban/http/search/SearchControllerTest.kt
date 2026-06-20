package com.kanban.http.search

import com.kanban.search.SearchOperation
import com.kanban.search.SearchResult
import io.mockk.coEvery
import io.mockk.coVerify
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

internal class SearchControllerTest : BaseSearchControllerTest() {
    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webClient = bindTo(SearchController::class.java)
    }

    @Test
    fun `should return 200 with search results`() {
        val now = Instant.now()
        val results =
            listOf(
                SearchResult(
                    id = "task-1",
                    title = "Found task",
                    description = "Description",
                    status = "",
                    priority = null,
                    assigneeId = "user-1",
                    boardId = "board-1",
                    columnId = "column-1",
                    projectId = "project-1",
                    dueDate = null,
                    createdAt = now,
                    updatedAt = now,
                    rank = 0.5f,
                ),
            )

        coEvery {
            searchOperation.execute(any())
        } returns SearchOperation.Result.Success(results = results, total = 1L)

        webClient
            .get()
            .uri { builder ->
                builder
                    .path("/api/v1/search")
                    .queryParam("q", "test")
                    .queryParam("board_id", "board-1")
                    .queryParam("assignee_id", "user-1")
                    .build()
            }.accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.results.length()")
            .isEqualTo(1)
            .jsonPath("$.results[0].id")
            .isEqualTo("task-1")
            .jsonPath("$.results[0].title")
            .isEqualTo("Found task")
            .jsonPath("$.results[0].board_id")
            .isEqualTo("board-1")
            .jsonPath("$.total")
            .isEqualTo(1)

        coVerify {
            searchOperation.execute(
                match {
                    it.criteria.query == "test" &&
                        it.criteria.boardId == "board-1" &&
                        it.criteria.assigneeId == "user-1"
                },
            )
        }
    }

    @Test
    fun `should return 200 with empty results when nothing found`() {
        coEvery {
            searchOperation.execute(any())
        } returns SearchOperation.Result.Success(results = emptyList(), total = 0L)

        webClient
            .get()
            .uri { builder ->
                builder
                    .path("/api/v1/search")
                    .queryParam("q", "nonexistent")
                    .build()
            }.accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.results.length()")
            .isEqualTo(0)
            .jsonPath("$.total")
            .isEqualTo(0)
    }

    @Test
    fun `should pass pagination parameters`() {
        coEvery {
            searchOperation.execute(any())
        } returns SearchOperation.Result.Success(results = emptyList(), total = 0L)

        webClient
            .get()
            .uri { builder ->
                builder
                    .path("/api/v1/search")
                    .queryParam("q", "test")
                    .queryParam("page", "2")
                    .queryParam("size", "10")
                    .build()
            }.accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk

        coVerify {
            searchOperation.execute(
                match {
                    it.criteria.query == "test" &&
                        it.criteria.limit == 10 &&
                        it.criteria.offset == 20
                },
            )
        }
    }

    @Test
    fun `should pass all filter parameters`() {
        coEvery {
            searchOperation.execute(any())
        } returns SearchOperation.Result.Success(results = emptyList(), total = 0L)

        webClient
            .get()
            .uri { builder ->
                builder
                    .path("/api/v1/search")
                    .queryParam("q", "bug")
                    .queryParam("project_id", "proj-1")
                    .queryParam("board_id", "board-1")
                    .queryParam("status", "in_progress")
                    .queryParam("priority", "high")
                    .queryParam("assignee_id", "user-1")
                    .queryParam("due_date_from", "2024-01-01T00:00:00Z")
                    .queryParam("due_date_to", "2024-12-31T23:59:59Z")
                    .queryParam("page", "0")
                    .queryParam("size", "50")
                    .build()
            }.accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk

        coVerify {
            searchOperation.execute(
                match {
                    it.criteria.query == "bug" &&
                        it.criteria.projectId == "proj-1" &&
                        it.criteria.boardId == "board-1" &&
                        it.criteria.status == "in_progress" &&
                        it.criteria.priority == "high" &&
                        it.criteria.assigneeId == "user-1" &&
                        it.criteria.dueDateFrom != null &&
                        it.criteria.dueDateTo != null &&
                        it.criteria.limit == 50 &&
                        it.criteria.offset == 0
                },
            )
        }
    }

    @Test
    fun `should use default pagination when not specified`() {
        coEvery {
            searchOperation.execute(any())
        } returns SearchOperation.Result.Success(results = emptyList(), total = 0L)

        webClient
            .get()
            .uri { builder ->
                builder
                    .path("/api/v1/search")
                    .queryParam("q", "test")
                    .build()
            }.accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk

        coVerify {
            searchOperation.execute(
                match {
                    it.criteria.limit == 20 &&
                        it.criteria.offset == 0
                },
            )
        }
    }

    @Test
    fun `should handle empty query string`() {
        coEvery {
            searchOperation.execute(any())
        } returns SearchOperation.Result.Success(results = emptyList(), total = 0L)

        webClient
            .get()
            .uri { builder ->
                builder
                    .path("/api/v1/search")
                    .build()
            }.accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
    }
}
