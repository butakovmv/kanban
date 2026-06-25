package com.kanban.http.search

import com.kanban.http.ErrorResponse
import com.kanban.search.SearchHandler
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import java.time.Instant
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/search")
@Tag(name = "Search", description = "Search operations")
internal class SearchController(
    private val handler: SearchHandler,
) {
    @Operation(
        summary = "Search tasks",
        description = "Search tasks with optional filters",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Search results",
                content = [Content(schema = Schema(implementation = SearchResultWrapper::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid query parameters",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    @GetMapping
    @Suppress("LongParameterList")
    suspend fun search(
        @Parameter(description = "Search query") @RequestParam("q", required = false) query: String?,
        @Parameter(description = "Project ID filter") @RequestParam("project_id", required = false) projectId: String?,
        @Parameter(description = "Status filter") @RequestParam("status", required = false) status: String?,
        @Parameter(description = "Priority filter") @RequestParam("priority", required = false) priority: String?,
        @Parameter(description = "Assignee ID filter") @RequestParam("assignee_id", required = false) assigneeId: String?,
        @Parameter(description = "Due date from") @RequestParam("due_date_from", required = false) dueDateFrom: Instant?,
        @Parameter(description = "Due date to") @RequestParam("due_date_to", required = false) dueDateTo: Instant?,
        @Parameter(description = "Page number", example = "0") @RequestParam("page", defaultValue = "0") page: Int,
        @Parameter(description = "Page size", example = "20") @RequestParam("size", defaultValue = "20") size: Int,
    ): ResponseEntity<*> {
        val result =
            handler.search(
                query = query?.takeIf { it.isNotBlank() },
                projectId = projectId?.takeIf { it.isNotBlank() },
                status = status?.takeIf { it.isNotBlank() },
                priority = priority?.takeIf { it.isNotBlank() },
                assigneeId = assigneeId?.takeIf { it.isNotBlank() },
                dueDateFrom = dueDateFrom,
                dueDateTo = dueDateTo,
                page = page,
                size = size,
            )
        return ResponseEntity.ok(
            SearchResultWrapper(
                results =
                    result.results.map {
                        SearchItemResponse(
                            id = it.id,
                            title = it.title,
                            description = it.description,
                            status = it.status,
                            priority = it.priority,
                            assigneeId = it.assigneeId,
                            projectId = it.projectId,
                            columnId = it.columnId,
                            boardId = it.boardId,
                            dueDate = it.dueDate,
                            createdAt = it.createdAt,
                            updatedAt = it.updatedAt,
                            rank = it.rank,
                        )
                    },
                total = result.total,
            ),
        )
    }
}
