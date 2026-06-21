package com.kanban.http.document

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.document.DocumentHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/documents")
internal class CreateDocumentController(
    private val handler: DocumentHandler,
) {
    @PostMapping
    suspend fun create(
        @RequestBody body: CreateDocumentBody,
    ): ResponseEntity<*> {
        val result =
            handler.create(
                projectId = body.projectId,
                title = body.title,
                description = body.description,
                fileName = body.fileName,
                contentType = body.contentType,
                contentBase64 = body.contentBase64,
                uploadedBy = body.uploadedBy,
            )
        return when (result) {
            is DocumentHandler.CreateDocumentResult.Success ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                        DocumentResponse(
                            id = result.document.id,
                            projectId = result.document.projectId,
                            title = result.document.title,
                            description = result.document.description,
                            fileName = result.document.fileName,
                            contentType = result.document.contentType,
                            sizeBytes = result.document.sizeBytes,
                            storageKey = result.document.storageKey,
                            version = result.document.version,
                            uploadedBy = result.document.uploadedBy,
                            createdAt = result.document.createdAt,
                            updatedAt = result.document.updatedAt,
                        ),
                    )
            is DocumentHandler.CreateDocumentResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }

    data class CreateDocumentBody(
        @JsonProperty("project_id")
        val projectId: String,
        val title: String,
        val description: String?,
        @JsonProperty("file_name")
        val fileName: String,
        @JsonProperty("content_type")
        val contentType: String,
        @JsonProperty("content_base64")
        val contentBase64: String,
        @JsonProperty("uploaded_by")
        val uploadedBy: String,
    )
}
