package com.kanban.http.document

import com.kanban.document.DocumentHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/documents/{id}/download")
internal class DownloadDocumentController(
    private val handler: DocumentHandler,
) {
    @GetMapping
    suspend fun getDownloadUrl(
        @PathVariable("id") id: String,
    ): ResponseEntity<*> {
        val result = handler.getDownloadUrl(documentId = id)
        return when (result) {
            is DocumentHandler.GetDocumentDownloadUrlResult.Success ->
                ResponseEntity.ok(DocumentDownloadUrlResponse(url = result.url))
            DocumentHandler.GetDocumentDownloadUrlResult.NotFound ->
                ResponseEntity.notFound().build<Any>()
        }
    }
}
