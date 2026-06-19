package com.kanban.document

import com.kanban.common.Operation

/**
 * Операция создания нового документа в проекте.
 * Загружает содержимое во внешнее хранилище через [DocumentStorage] и сохраняет метаданные в репозитории
 * с начальной версией 1.
 */
interface CreateDocumentOperation : Operation<CreateDocumentOperation.Arg, CreateDocumentOperation.Result> {
    /**
     * Аргумент операции создания документа.
     *
     * @property projectId идентификатор проекта
     * @property title заголовок документа
     * @property description описание документа (опционально)
     * @property fileName исходное имя файла
     * @property contentType MIME-тип содержимого файла
     * @property content содержимое файла
     * @property uploadedBy идентификатор пользователя, загрузившего файл
     */
    data class Arg(
        val projectId: String,
        val title: String,
        val description: String?,
        val fileName: String,
        val contentType: String,
        val content: ByteArray,
        val uploadedBy: String,
    )

    /**
     * Результат операции создания документа.
     */
    sealed interface Result {
        /** Документ успешно создан. */
        data class Success(
            val document: Document,
        ) : Result

        /** Ошибка создания документа (валидация, проект не найден и т. п.). */
        data class Failure(
            val reason: String,
        ) : Result
    }
}
