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
        val result = handler.listBoardBacklog(boardId = boardId)
        return when (result) {
            is TaskHandler.ListBoardBacklogResult.Success ->
                ResponseEntity.ok(
                    mapOf(
                        "tasks" to result.tasks.map { task ->
                            TaskResponse(
                                id = task.id,
                                boardId = task.boardId,
                                columnId = task.columnId,
                                title = task.title,
                                description = task.description,
                                assigneeId = task.assigneeId,
                                position = task.position,
                                dueDate = task.dueDate,
                                archived = task.archived,
                                createdAt = task.createdAt,
                                updatedAt = task.updatedAt,
                            )
                        },
                    ),
                )
        }
    }
}
