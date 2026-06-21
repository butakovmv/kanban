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
@RequestMapping("/api/v1/documents/{id}")
internal class UpdateDocumentController(
    private val handler: DocumentHandler,
) {
    @PutMapping
    suspend fun update(
        @PathVariable("id") id: String,
        @RequestBody body: UpdateDocumentBody,
    ): ResponseEntity<*> {
        val result = handler.update(documentId = id, title = body.title, description = body.description)
        return when (result) {
            is DocumentHandler.UpdateDocumentResult.Success ->
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
            DocumentHandler.UpdateDocumentResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
            is DocumentHandler.UpdateDocumentResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }

    data class UpdateDocumentBody(
        val title: String?,
        val description: String?,
    )
}
