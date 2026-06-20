package com.kanban.http.search

import com.kanban.search.SearchHandler
import java.time.Instant
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/search")
internal class SearchController(
    private val handler: SearchHandler,
) {
    @GetMapping
    @Suppress("LongParameterList")
    suspend fun search(
        @RequestParam("q", required = false) query: String?,
        @RequestParam("project_id", required = false) projectId: String?,
        @RequestParam("board_id", required = false) boardId: String?,
        @RequestParam("status", required = false) status: String?,
        @RequestParam("priority", required = false) priority: String?,
        @RequestParam("assignee_id", required = false) assigneeId: String?,
        @RequestParam("due_date_from", required = false) dueDateFrom: Instant?,
        @RequestParam("due_date_to", required = false) dueDateTo: Instant?,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "20") size: Int,
    ): ResponseEntity<*> {
        val request =
            SearchHandler.SearchRequest(
                query = query?.takeIf { it.isNotBlank() },
                projectId = projectId?.takeIf { it.isNotBlank() },
                boardId = boardId?.takeIf { it.isNotBlank() },
                status = status?.takeIf { it.isNotBlank() },
                priority = priority?.takeIf { it.isNotBlank() },
                assigneeId = assigneeId?.takeIf { it.isNotBlank() },
                dueDateFrom = dueDateFrom,
                dueDateTo = dueDateTo,
                page = page,
                size = size,
            )
        val result = handler.search(request)
        return ResponseEntity.ok(result)
    }
}
