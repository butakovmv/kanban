package com.kanban.task

import com.kanban.common.Operation

/**
 * Операция прикрепления файла к задаче.
 * Загружает содержимое во внешнее хранилище через [FileStorage] и сохраняет метаданные в репозитории.
 */
interface AttachFileOperation : Operation<AttachFileOperation.Arg, AttachFileOperation.Result> {
    /**
     * Аргумент операции прикрепления файла.
     *
     * @property taskId идентификатор задачи
     * @property fileName имя файла
     * @property contentType MIME-тип содержимого
     * @property sizeBytes размер файла в байтах
     * @property content содержимое файла
     * @property uploadedBy идентификатор пользователя, загрузившего файл
     */
    data class Arg(
        val taskId: String,
        val fileName: String,
        val contentType: String,
        val sizeBytes: Long,
        val content: ByteArray,
        val uploadedBy: String,
    )

    /**
     * Результат операции прикрепления файла.
     */
    sealed interface Result {
        /** Файл успешно загружен и прикреплён к задаче. */
        data class Success(
            val file: FileAttachment,
        ) : Result

        /** Ошибка прикрепления (задача не найдена, не удалось загрузить в хранилище и т. п.). */
        data class Failure(
            val reason: String,
        ) : Result
    }
}
