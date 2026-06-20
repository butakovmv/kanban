package com.kanban.http.task

import com.kanban.task.TaskHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/boards/{boardId}/backlog")
internal class ListBoardBacklogController(
    private val handler: TaskHandler,
) {
    @GetMapping
    suspend fun listBacklog(
        @PathVariable("boardId") boardId: String,
    ): ResponseEntity<*> {
        val request = TaskHandler.ListBoardBacklogRequest(boardId = boardId)
        val result = handler.listBoardBacklog(request)
        return when (result) {
            is TaskHandler.ListBoardBacklogResult.Success ->
                ResponseEntity.ok(mapOf("tasks" to result.tasks))
        }
    }
}
