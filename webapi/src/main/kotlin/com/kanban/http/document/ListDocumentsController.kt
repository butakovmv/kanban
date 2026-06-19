package com.kanban.http.document

import com.kanban.document.DocumentHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер получения списка документов проекта.
 * Обрабатывает только запрос `GET /api/v1/projects/{projectId}/documents`.
 *
 * @property handler обработчик запросов документов
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/documents")
internal class ListDocumentsController(
    private val handler: DocumentHandler,
) {
    /**
     * Возвращает список документов проекта, упорядоченный по дате последнего изменения (DESC).
     *
     * @param projectId идентификатор проекта
     * @return 200 со списком документов
     */
    @GetMapping
    suspend fun list(
        @PathVariable("projectId") projectId: String,
    ): ResponseEntity<*> {
        val request = DocumentHandler.ListDocumentsRequest(projectId = projectId)
        val result = handler.list(request)
        return when (result) {
            is DocumentHandler.ListDocumentsResult.Success ->
                ResponseEntity.ok(mapOf("documents" to result.documents))
        }
    }
}
