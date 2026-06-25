package com.kanban.http.task

import com.kanban.task.TaskHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/projects/{projectId}/tasks")
internal class ListTasksController(
    private val handler: TaskHandler,
) {
    @GetMapping
    suspend fun list(
        @PathVariable("projectId") projectId: String,
        @RequestParam("include_archived", defaultValue = "false") includeArchived: Boolean,
    ): ResponseEntity<*> {
        val result = handler.list(
            projectId = projectId,
            includeArchived = includeArchived,
        )
        return when (result) {
            is TaskHandler.ListTasksResult.Success ->
                ResponseEntity.ok(
                    mapOf(
                        "tasks" to result.tasks.map { task ->
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
                            )
                        },
                    ),
                )
        }
    }
}
