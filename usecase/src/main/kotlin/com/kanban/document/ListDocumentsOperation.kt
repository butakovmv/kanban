package com.kanban.document

import com.kanban.common.Operation

/**
 * Операция получения списка документов проекта.
 * Документы возвращаются упорядоченными по дате последнего изменения (DESC).
 */
interface ListDocumentsOperation : Operation<ListDocumentsOperation.Arg, ListDocumentsOperation.Result> {
    /**
     * Аргумент операции получения списка документов.
     *
     * @property projectId идентификатор проекта
     */
    data class Arg(
        val projectId: String,
    )

    /**
     * Результат операции получения списка документов.
     */
    sealed interface Result {
        /** Список документов успешно получен. */
        data class Success(
            val documents: List<Document>,
        ) : Result
    }
}
