package com.kanban.http.task

import com.kanban.task.LabelHandler
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/tasks/{taskId}/labels/{label}")
@Tag(name = "Tasks", description = "Task management operations")
internal class RemoveTaskLabelController(
    private val handler: LabelHandler,
) {
    @DeleteMapping
    suspend fun remove(
        @PathVariable("taskId") taskId: String,
        @PathVariable("label") label: String,
    ): ResponseEntity<*> {
        val result = handler.remove(taskId = taskId, label = label)
        return when (result) {
            LabelHandler.RemoveLabelResult.Success ->
                ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build<Any>()
        }
    }
}
