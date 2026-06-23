package com.kanban.http.document

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
        val result =
            handler.update(
                documentId = id,
                path = body.path,
                title = body.title,
                content = body.content,
                description = body.description,
            )
        return when (result) {
            is DocumentHandler.UpdateDocumentResult.Success ->
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
            DocumentHandler.UpdateDocumentResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
            is DocumentHandler.UpdateDocumentResult.Failure ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("reason" to result.reason))
        }
    }

    data class UpdateDocumentBody(
        val path: String?,
        val title: String?,
        val content: String?,
        val description: String?,
    )
}
