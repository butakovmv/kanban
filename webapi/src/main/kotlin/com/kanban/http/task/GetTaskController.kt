package com.kanban.http.task

import com.kanban.http.NotFoundErrorResponse
import com.kanban.task.TaskHandler
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/tasks/{id}")
@Tag(name = "Tasks", description = "Task management operations")
internal class GetTaskController(
    private val handler: TaskHandler,
) {
    @Operation(summary = "Get a task by ID")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Task found",
                content = [Content(schema = Schema(implementation = TaskResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Task not found",
                content = [Content(schema = Schema(implementation = NotFoundErrorResponse::class))],
            ),
        ],
    )
    @GetMapping
    suspend fun get(
        @Parameter(description = "Task ID") @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val result = handler.get(taskId = id)
        return when (result) {
            is TaskHandler.GetTaskResult.Success -> {
                val task = result.task
                ResponseEntity.ok(
                    TaskResponse(
                        id = task.id,
                        projectId = task.projectId,
                        columnId = task.columnId,
                        title = task.title,
                        description = task.description,
                        assigneeId = task.assigneeId,
                        position = task.position,
                        dueDate = task.dueDate,
                        priority = task.priority,
                        archived = task.archived,
                        createdAt = task.createdAt,
                        updatedAt = task.updatedAt,
                        labels = task.labels,
                    ),
                )
            }
            TaskHandler.GetTaskResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
