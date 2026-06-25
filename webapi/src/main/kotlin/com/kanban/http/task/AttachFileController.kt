package com.kanban.http.task

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.task.FileHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/tasks/{taskId}/files")
internal class AttachFileController(
    private val handler: FileHandler,
) {
    data class AttachFileBody(
        @JsonProperty("file_name")
        val fileName: String,
        @JsonProperty("content_type")
        val contentType: String,
        @JsonProperty("content_base64")
        val contentBase64: String,
        @JsonProperty("uploaded_by")
        val uploadedBy: String,
    )

    @PostMapping
    suspend fun attach(
        @PathVariable("taskId") taskId: String,
        @RequestBody body: AttachFileBody,
    ): ResponseEntity<*> {
        val result =
            handler.attach(
                taskId = taskId,
                fileName = body.fileName,
                contentType = body.contentType,
                contentBase64 = body.contentBase64,
                uploadedBy = body.uploadedBy,
            )
        return when (result) {
            is FileHandler.AttachFileResult.Success -> {
                val file = result.file
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                        FileAttachmentResponse(
                            id = file.id,
                            taskId = file.taskId,
                            fileName = file.fileName,
                            contentType = file.contentType,
                            sizeBytes = file.sizeBytes,
                            storageKey = file.storageKey,
                            uploadedBy = file.uploadedBy,
                            uploadedAt = file.uploadedAt,
                        ),
                    )
            }
            is FileHandler.AttachFileResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }
}
