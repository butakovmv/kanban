package com.kanban.http.task

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.http.NotFoundErrorResponse
import com.kanban.task.TaskHandler
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/tasks/{id}")
@Tag(name = "Tasks", description = "Task management operations")
internal class DeleteTaskController(
    private val handler: TaskHandler,
) {
    data class DeleteBody(
        @JsonProperty("user_id")
        @field:Schema(description = "User performing action", hidden = true)
        val userId: String?,
    )

    @Operation(summary = "Delete a task")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            ApiResponse(
                responseCode = "404",
                description = "Task not found",
                content = [Content(schema = Schema(implementation = NotFoundErrorResponse::class))],
            ),
        ],
    )
    @DeleteMapping
    suspend fun delete(
        @Parameter(description = "Task ID") @PathVariable("id") id: String,
        @RequestBody(required = false) body: DeleteBody?,
    ): ResponseEntity<*> {
        val taskResult = handler.get(taskId = id)
        val projectId =
            when (taskResult) {
                is TaskHandler.GetTaskResult.Success -> taskResult.task.projectId
                else -> null
            }
        val result = handler.delete(taskId = id, userId = body?.userId, projectId = projectId)
        return when (result) {
            TaskHandler.DeleteTaskResult.Success ->
                ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build<Any>()
            TaskHandler.DeleteTaskResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
