package com.kanban.http.task

import com.kanban.task.FileHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/tasks/{taskId}/files")
internal class ListFilesController(
    private val handler: FileHandler,
) {
    @GetMapping
    suspend fun list(
        @PathVariable("taskId") taskId: String,
    ): ResponseEntity<*> {
        val result = handler.list(taskId = taskId)
        return when (result) {
            is FileHandler.ListFilesResult.Success ->
                ResponseEntity.ok(
                    mapOf(
                        "files" to
                            result.files.map { file ->
                                FileAttachmentResponse(
                                    id = file.id,
                                    taskId = file.taskId,
                                    fileName = file.fileName,
                                    contentType = file.contentType,
                                    sizeBytes = file.sizeBytes,
                                    storageKey = file.storageKey,
                                    uploadedBy = file.uploadedBy,
                                    uploadedAt = file.uploadedAt,
                                )
                            },
                    ),
                )
        }
    }
}
