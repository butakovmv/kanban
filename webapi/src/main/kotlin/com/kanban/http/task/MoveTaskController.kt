package com.kanban.http.task

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.http.ErrorResponse
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
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/tasks/{id}/move")
@Tag(name = "Tasks", description = "Task management operations")
internal class MoveTaskController(
    private val handler: TaskHandler,
) {
    data class MoveTaskBody(
        @JsonProperty("column_id")
        @field:Schema(description = "Target column identifier", example = "550e8400-e29b-41d4-a716-446655440002")
        val columnId: String,
        @field:Schema(description = "Position in target column", example = "0")
        val position: Int,
        @JsonProperty("user_id")
        @field:Schema(description = "User performing action", hidden = true)
        val userId: String?,
    )

    @Operation(
        summary = "Move a task",
        description = "Moves a task to a different column and/or position",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Task moved successfully",
                content = [Content(schema = Schema(implementation = TaskResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request body or validation error",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Task not found",
                content = [Content(schema = Schema(implementation = NotFoundErrorResponse::class))],
            ),
        ],
    )
    @PatchMapping
    suspend fun move(
        @Parameter(description = "Task ID") @PathVariable("id") id: String,
        @RequestBody body: MoveTaskBody,
    ): ResponseEntity<*> {
        val result = handler.move(
            taskId = id,
            columnId = body.columnId,
            position = body.position,
            userId = body.userId,
        )
        return when (result) {
            is TaskHandler.MoveTaskResult.Success -> {
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
            TaskHandler.MoveTaskResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
