package com.kanban.document

import com.kanban.common.Operation

/**
 * Операция обновления метаданных документа (заголовок и/или описание).
 * Изменяет только метаданные, не затрагивает содержимое файла и не увеличивает номер версии.
 * Значение null в любом из обновляемых полей означает «не изменять соответствующее поле».
 */
interface UpdateDocumentOperation : Operation<UpdateDocumentOperation.Arg, UpdateDocumentOperation.Result> {
    /**
     * Аргумент операции обновления документа.
     *
     * @property documentId идентификатор документа
     * @property title новый заголовок (null — не изменять)
     * @property description новое описание (null — не изменять)
     */
    data class Arg(
        val documentId: String,
        val title: String?,
        val description: String?,
    )

    /**
     * Результат операции обновления документа.
     */
    sealed interface Result {
        /** Документ успешно обновлён. */
        data class Success(
            val document: Document,
        ) : Result

        /** Документ не найден. */
        data object NotFound : Result

        /** Ошибка обновления (валидация входных данных). */
        data class Failure(
            val reason: String,
        ) : Result
    }
}
