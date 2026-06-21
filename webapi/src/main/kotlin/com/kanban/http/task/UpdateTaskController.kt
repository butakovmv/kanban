package com.kanban.http.task

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.task.TaskHandler
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
internal class UpdateTaskController(
    private val handler: TaskHandler,
) {
    data class UpdateTaskBody(
        val title: String?,
        val description: String?,
        @JsonProperty("assignee_id")
        val assigneeId: String?,
        @JsonProperty("due_date")
        val dueDate: Instant?,
    )

    @PutMapping
    suspend fun update(
        @PathVariable("id") id: String,
        @RequestBody body: UpdateTaskBody,
    ): ResponseEntity<*> {
        val result = handler.update(
            taskId = id,
            title = body.title,
            description = body.description,
            assigneeId = body.assigneeId,
            dueDate = body.dueDate,
        )
        return when (result) {
            is TaskHandler.UpdateTaskResult.Success -> {
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
            TaskHandler.UpdateTaskResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
            is TaskHandler.UpdateTaskResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }
}
