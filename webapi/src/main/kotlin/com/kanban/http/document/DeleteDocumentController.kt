package com.kanban.http.document

import com.kanban.document.DocumentHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/documents/{id}")
internal class DeleteDocumentController(
    private val handler: DocumentHandler,
) {
    @DeleteMapping
    suspend fun delete(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val result = handler.delete(documentId = id)
        return when (result) {
            DocumentHandler.DeleteDocumentResult.Success ->
                ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build<Any>()
            DocumentHandler.DeleteDocumentResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
