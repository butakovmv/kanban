package com.kanban.http.document

import java.util.Base64
import java.util.UUID

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

    fun updateDocumentBody(): UpdateDocumentController.UpdateDocumentBody =
        UpdateDocumentController.UpdateDocumentBody(
            title = "Updated ${UUID.randomUUID().toString().take(6)}",
            description = "Updated ${UUID.randomUUID().toString().take(6)}",
        )

    fun replaceDocumentBody(): ReplaceDocumentController.ReplaceDocumentBody =
        ReplaceDocumentController.ReplaceDocumentBody(
            contentBase64 = Base64.getEncoder().encodeToString("new-content".toByteArray()),
            fileName = "new-${UUID.randomUUID().toString().take(6)}.pdf",
            contentType = "application/pdf",
        )

    fun replaceDocumentBodyWithoutOptionals(): ReplaceDocumentController.ReplaceDocumentBody =
        ReplaceDocumentController.ReplaceDocumentBody(
            contentBase64 = Base64.getEncoder().encodeToString("new-content".toByteArray()),
            fileName = null,
            contentType = null,
        )
}
