package com.kanban.document

import com.kanban.common.Operation

/**
 * Операция получения документа по идентификатору.
 */
interface GetDocumentOperation : Operation<GetDocumentOperation.Arg, GetDocumentOperation.Result> {
    /**
     * Аргумент операции получения документа.
     *
     * @property documentId идентификатор документа
     */
    data class Arg(
        val documentId: String,
    )

    /**
     * Результат операции получения документа.
     */
    sealed interface Result {
        /** Документ найден. */
        data class Success(
            val document: Document,
        ) : Result

        /** Документ не найден. */
        data object NotFound : Result
    }
}
