package com.kanban.http.task

import com.kanban.task.LabelHandler
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/tasks/{taskId}/labels")
@Tag(name = "Tasks", description = "Task management operations")
internal class AddTaskLabelController(
    private val handler: LabelHandler,
) {
    data class AddLabelBody(
        val label: String,
    )

    @PostMapping
    suspend fun add(
        @PathVariable("taskId") taskId: String,
        @RequestBody body: AddLabelBody,
    ): ResponseEntity<*> {
        val result = handler.add(taskId = taskId, label = body.label)
        return when (result) {
            LabelHandler.AddLabelResult.Success ->
                ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build<Any>()
        }
    }
}
