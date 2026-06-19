package com.kanban.http.document

import com.kanban.document.DocumentHandler
import java.util.Base64
import java.util.UUID

/**
 * Генератор тестовых DTO для запросов документов.
 * Создаёт случайные данные, которые используются в тестах контроллеров.
 */
internal object RequestGenerator {
    fun createDocumentBody(): CreateDocumentController.CreateDocumentBody =
        CreateDocumentController.CreateDocumentBody(
            projectId = "project-${UUID.randomUUID()}",
            title = "Document ${UUID.randomUUID().toString().take(6)}",
            description = "Description ${UUID.randomUUID().toString().take(6)}",
            fileName = "file-${UUID.randomUUID().toString().take(6)}.pdf",
            contentType = "application/pdf",
            contentBase64 = Base64.getEncoder().encodeToString("test-content".toByteArray()),
            uploadedBy = "user-${UUID.randomUUID()}",
        )

    fun createDocumentBodyWithoutDescription(): CreateDocumentController.CreateDocumentBody =
        createDocumentBody().copy(
            description = null,
        )

    fun updateDocumentBody(): DocumentHandler.UpdateDocumentBody =
        DocumentHandler.UpdateDocumentBody(
            title = "Updated ${UUID.randomUUID().toString().take(6)}",
            description = "Updated ${UUID.randomUUID().toString().take(6)}",
        )

    fun replaceDocumentBody(): DocumentHandler.ReplaceDocumentBody =
        DocumentHandler.ReplaceDocumentBody(
            contentBase64 = Base64.getEncoder().encodeToString("new-content".toByteArray()),
            fileName = "new-${UUID.randomUUID().toString().take(6)}.pdf",
            contentType = "application/pdf",
        )

    fun replaceDocumentBodyWithoutOptionals(): DocumentHandler.ReplaceDocumentBody =
        DocumentHandler.ReplaceDocumentBody(
            contentBase64 = Base64.getEncoder().encodeToString("new-content".toByteArray()),
            fileName = null,
            contentType = null,
        )
}
