package com.kanban.http.document

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.document.DocumentHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/documents/{id}/content")
internal class ReplaceDocumentController(
    private val handler: DocumentHandler,
) {
    @PutMapping
    suspend fun replace(
        @PathVariable("id") id: String,
        @RequestBody body: ReplaceDocumentBody,
    ): ResponseEntity<*> {
        val result =
            handler.replace(
                documentId = id,
                contentBase64 = body.contentBase64,
                fileName = body.fileName,
                contentType = body.contentType,
            )
        return when (result) {
            is DocumentHandler.ReplaceDocumentResult.Success ->
                ResponseEntity.ok(
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
            DocumentHandler.ReplaceDocumentResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
            is DocumentHandler.ReplaceDocumentResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }

    data class ReplaceDocumentBody(
        @JsonProperty("content_base64")
        val contentBase64: String,
        @JsonProperty("file_name")
        val fileName: String?,
        @JsonProperty("content_type")
        val contentType: String?,
    )
}
