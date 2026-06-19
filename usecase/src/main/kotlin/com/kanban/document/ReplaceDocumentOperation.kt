package com.kanban.document

import com.kanban.common.Operation

/**
 * Операция замены содержимого документа.
 * Загружает новое содержимое во внешнее хранилище, удаляет предыдущий объект,
 * обновляет имя файла и/или MIME-тип (если переданы) и увеличивает номер версии.
 */
interface ReplaceDocumentOperation : Operation<ReplaceDocumentOperation.Arg, ReplaceDocumentOperation.Result> {
    /**
     * Аргумент операции замены содержимого документа.
     *
     * @property documentId идентификатор документа
     * @property content новое содержимое файла
     * @property newFileName новое имя файла (null — не изменять)
     * @property newContentType новый MIME-тип содержимого (null — не изменять)
     */
    data class Arg(
        val documentId: String,
        val content: ByteArray,
        val newFileName: String?,
        val newContentType: String?,
    )

    /**
     * Результат операции замены содержимого документа.
     */
    sealed interface Result {
        /** Содержимое документа успешно заменено. */
        data class Success(
            val document: Document,
        ) : Result

        /** Документ не найден. */
        data object NotFound : Result

        /** Ошибка замены (валидация входных данных). */
        data class Failure(
            val reason: String,
        ) : Result
    }
}
