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
import java.time.Instant
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/tasks/{id}")
@Tag(name = "Tasks", description = "Task management operations")
internal class UpdateTaskController(
    private val handler: TaskHandler,
) {
    data class UpdateTaskBody(
        @field:Schema(description = "Task title", example = "Updated title")
        val title: String?,
        @field:Schema(description = "Task description", example = "Updated description", nullable = true)
        val description: String?,
        @JsonProperty("assignee_id")
        @field:Schema(description = "Assignee user identifier", example = "550e8400-e29b-41d4-a716-446655440003", nullable = true)
        val assigneeId: String?,
        @JsonProperty("due_date")
        @field:Schema(description = "Due date", example = "2024-12-31T23:59:59Z", nullable = true)
        val dueDate: Instant?,
        @field:Schema(description = "Priority", example = "high", nullable = true)
        val priority: String?,
        @JsonProperty("user_id")
        @field:Schema(description = "User performing action", hidden = true)
        val userId: String?,
    )

    @Operation(
        summary = "Update a task",
        description = "Updates an existing task with the provided fields",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Task updated successfully",
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
    @PutMapping
    suspend fun update(
        @Parameter(description = "Task ID") @PathVariable("id") id: String,
        @RequestBody body: UpdateTaskBody,
    ): ResponseEntity<*> {
        val taskResult = handler.get(taskId = id)
        val taskProjectId =
            when (taskResult) {
                is TaskHandler.GetTaskResult.Success -> taskResult.task.projectId
                else -> null
            }
        val result =
            handler.update(
                taskId = id,
                title = body.title,
                description = body.description,
                assigneeId = body.assigneeId,
                dueDate = body.dueDate,
                priority = body.priority,
                userId = body.userId,
                projectId = taskProjectId,
            )
        return when (result) {
            is TaskHandler.UpdateTaskResult.Success -> {
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
            TaskHandler.UpdateTaskResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
            is TaskHandler.UpdateTaskResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }
}
