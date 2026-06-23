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
                path = body.path,
                title = body.title,
                content = body.content,
                description = body.description,
            )
        return when (result) {
            is DocumentHandler.CreateDocumentResult.Success ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                        DocumentResponse(
                            id = result.document.id,
                            projectId = result.document.projectId,
                            path = result.document.path,
                            title = result.document.title,
                            description = result.document.description,
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
        val path: String,
        val title: String,
        val content: String,
        val description: String?,
    )
}
