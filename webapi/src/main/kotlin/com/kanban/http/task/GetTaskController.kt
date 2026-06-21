package com.kanban.http.task

import com.kanban.task.TaskHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/tasks/{id}")
internal class GetTaskController(
    private val handler: TaskHandler,
) {
    @GetMapping
    suspend fun get(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val result = handler.get(taskId = id)
        return when (result) {
            is TaskHandler.GetTaskResult.Success -> {
                val task = result.task
                ResponseEntity.ok(
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
                    ),
                )
            }
            TaskHandler.GetTaskResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
