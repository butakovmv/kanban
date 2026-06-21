package com.kanban.http.task

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.task.TaskHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/tasks/{id}/move")
internal class MoveTaskController(
    private val handler: TaskHandler,
) {
    data class MoveTaskBody(
        @JsonProperty("column_id")
        val columnId: String,
        val position: Int,
    )

    @PatchMapping
    suspend fun move(
        @PathVariable("id") id: String,
        @RequestBody body: MoveTaskBody,
    ): ResponseEntity<*> {
        val result = handler.move(
            taskId = id,
            columnId = body.columnId,
            position = body.position,
        )
        return when (result) {
            is TaskHandler.MoveTaskResult.Success -> {
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
            TaskHandler.MoveTaskResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
