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
                    DocumentDetailResponse(
                        id = result.document.id,
                        projectId = result.document.projectId,
                        path = result.document.path,
                        title = result.document.title,
                        content = result.document.content,
                        description = result.document.description,
                        createdAt = result.document.createdAt,
                        updatedAt = result.document.updatedAt,
                    ),
                )
            DocumentHandler.GetDocumentResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
