package com.kanban.http.task

import com.kanban.task.TaskHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/tasks/{id}")
internal class DeleteTaskController(
    private val handler: TaskHandler,
) {
    @DeleteMapping
    suspend fun delete(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val result = handler.delete(taskId = id)
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
