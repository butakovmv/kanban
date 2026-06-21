package com.kanban.http.task

import com.kanban.task.TaskHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/tasks/{id}/archive")
internal class ArchiveTaskController(
    private val handler: TaskHandler,
) {
    @PostMapping
    suspend fun archive(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val result = handler.archive(taskId = id)
        return when (result) {
            TaskHandler.ArchiveTaskResult.Success ->
                ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build<Any>()
            TaskHandler.ArchiveTaskResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
