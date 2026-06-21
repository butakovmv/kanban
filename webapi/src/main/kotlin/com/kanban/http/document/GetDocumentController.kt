package com.kanban.http.document

import com.kanban.document.DocumentHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/documents/{id}")
internal class GetDocumentController(
    private val handler: DocumentHandler,
) {
    @GetMapping
    suspend fun get(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val result = handler.get(documentId = id)
        return when (result) {
            is DocumentHandler.GetDocumentResult.Success ->
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
            DocumentHandler.GetDocumentResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
