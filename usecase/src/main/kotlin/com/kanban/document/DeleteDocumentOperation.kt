package com.kanban.document

import com.kanban.common.Operation

/**
 * Операция удаления документа.
 * Удаляет объект из внешнего хранилища и запись о документе из репозитория.
 */
interface DeleteDocumentOperation : Operation<DeleteDocumentOperation.Arg, DeleteDocumentOperation.Result> {
    /**
     * Аргумент операции удаления документа.
     *
     * @property documentId идентификатор документа
     */
    data class Arg(
        val documentId: String,
    )

    /**
     * Результат операции удаления документа.
     */
    sealed interface Result {
        /** Документ успешно удалён. */
        data object Success : Result

        /** Документ не найден. */
        data object NotFound : Result
    }
}
