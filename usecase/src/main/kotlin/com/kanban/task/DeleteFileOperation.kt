package com.kanban.task

import com.kanban.common.Operation

/**
 * Операция удаления прикреплённого файла.
 * Удаляет объект из хранилища и запись о прикреплении из репозитория.
 */
interface DeleteFileOperation : Operation<DeleteFileOperation.Arg, DeleteFileOperation.Result> {
    /**
     * Аргумент операции удаления файла.
     *
     * @property fileId идентификатор прикрепления
     */
    data class Arg(
        val fileId: String,
    )

    /**
     * Результат операции удаления файла.
     */
    sealed interface Result {
        /** Файл успешно удалён. */
        data object Success : Result

        /** Файл не найден. */
        data object NotFound : Result
    }
}
