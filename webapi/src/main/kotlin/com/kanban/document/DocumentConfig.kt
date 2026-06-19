package com.kanban.document

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Конфигурация доменного обработчика документов.
 * Регистрирует [DocumentHandler] как Spring-бин, связывая его с usecase-операциями и портом хранилища.
 */
@Configuration
internal class DocumentConfig {
    /**
     * Создаёт обработчик запросов документов.
     *
     * @param createDocumentOperation операция создания документа
     * @param getDocumentOperation операция получения документа
     * @param listDocumentsOperation операция получения списка документов
     * @param updateDocumentOperation операция обновления документа
     * @param replaceDocumentOperation операция замены содержимого документа
     * @param deleteDocumentOperation операция удаления документа
     * @param documentStorage порт хранилища документов
     * @return экземпляр [DocumentHandler]
     */
    @Bean
    @Suppress("LongParameterList")
    fun documentHandler(
        createDocumentOperation: CreateDocumentOperation,
        getDocumentOperation: GetDocumentOperation,
        listDocumentsOperation: ListDocumentsOperation,
        updateDocumentOperation: UpdateDocumentOperation,
        replaceDocumentOperation: ReplaceDocumentOperation,
        deleteDocumentOperation: DeleteDocumentOperation,
        documentStorage: DocumentStorage,
    ): DocumentHandler =
        DocumentHandler(
            createDocumentOperation = createDocumentOperation,
            getDocumentOperation = getDocumentOperation,
            listDocumentsOperation = listDocumentsOperation,
            updateDocumentOperation = updateDocumentOperation,
            replaceDocumentOperation = replaceDocumentOperation,
            deleteDocumentOperation = deleteDocumentOperation,
            documentStorage = documentStorage,
        )
}
