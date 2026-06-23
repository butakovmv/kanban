package com.kanban.http.document

import com.kanban.document.DocumentHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/projects/{projectId}/documents")
internal class ListDocumentsController(
    private val handler: DocumentHandler,
) {
    @GetMapping
    suspend fun list(
        @PathVariable("projectId") projectId: String,
    ): ResponseEntity<*> {
        val result = handler.list(projectId = projectId)
        return when (result) {
            is DocumentHandler.ListDocumentsResult.Success ->
                ResponseEntity.ok(
                    mapOf(
                        "documents" to result.documents.map {
                            DocumentResponse(
                                id = it.id,
                                projectId = it.projectId,
                                path = it.path,
                                title = it.title,
                                description = it.description,
                                createdAt = it.createdAt,
                                updatedAt = it.updatedAt,
                            )
                        },
                    ),
                )
        }
    }
}
