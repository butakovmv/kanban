package com.kanban.http.task

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.http.ErrorResponse
import com.kanban.task.TaskHandler
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import java.time.Instant
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Tasks", description = "Task management operations")
internal class CreateTaskController(
    private val handler: TaskHandler,
) {
    data class CreateTaskBody(
        @JsonProperty("project_id")
        @field:Schema(description = "Project identifier", example = "550e8400-e29b-41d4-a716-446655440001")
        val projectId: String,
        @JsonProperty("column_id")
        @field:Schema(description = "Column identifier", example = "550e8400-e29b-41d4-a716-446655440002")
        val columnId: String,
        @field:Schema(description = "Task title", example = "Implement login feature")
        val title: String,
        @field:Schema(description = "Task description", example = "Add OAuth2 login support", nullable = true)
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
        summary = "Create a new task",
        description = "Creates a new task in the specified project and column",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Task created successfully",
                content = [Content(schema = Schema(implementation = TaskResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request body or validation error",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    @PostMapping
    suspend fun create(
        @RequestBody body: CreateTaskBody,
    ): ResponseEntity<*> {
        val result = handler.create(
            projectId = body.projectId,
            columnId = body.columnId,
            title = body.title,
            description = body.description,
            assigneeId = body.assigneeId,
            dueDate = body.dueDate,
            priority = body.priority,
            userId = body.userId,
        )
        return when (result) {
            is TaskHandler.CreateTaskResult.Success -> {
                val task = result.task
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
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
                        ),
                    )
            }
            is TaskHandler.CreateTaskResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }
}
