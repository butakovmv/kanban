package com.kanban.http.task

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.task.TaskHandler
import java.time.Instant
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/tasks")
internal class CreateTaskController(
    private val handler: TaskHandler,
) {
    data class CreateTaskBody(
        @JsonProperty("board_id")
        val boardId: String,
        @JsonProperty("column_id")
        val columnId: String,
        val title: String,
        val description: String?,
        @JsonProperty("assignee_id")
        val assigneeId: String?,
        @JsonProperty("due_date")
        val dueDate: Instant?,
    )

    @PostMapping
    suspend fun create(
        @RequestBody body: CreateTaskBody,
    ): ResponseEntity<*> {
        val result = handler.create(
            boardId = body.boardId,
            columnId = body.columnId,
            title = body.title,
            description = body.description,
            assigneeId = body.assigneeId,
            dueDate = body.dueDate,
        )
        return when (result) {
            is TaskHandler.CreateTaskResult.Success -> {
                val task = result.task
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
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
            is TaskHandler.CreateTaskResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }
}
