package com.kanban.document

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
internal class DocumentConfig {
    @Bean
    fun documentHandler(
        createDocumentOperation: CreateDocumentOperation,
        getDocumentOperation: GetDocumentOperation,
        listDocumentsOperation: ListDocumentsOperation,
        updateDocumentOperation: UpdateDocumentOperation,
        deleteDocumentOperation: DeleteDocumentOperation,
    ): DocumentHandler =
        DocumentHandler(
            createDocumentOperation = createDocumentOperation,
            getDocumentOperation = getDocumentOperation,
            listDocumentsOperation = listDocumentsOperation,
            updateDocumentOperation = updateDocumentOperation,
            deleteDocumentOperation = deleteDocumentOperation,
        )
}
