package com.kanban.task

import com.kanban.common.Operation

/**
 * Операция получения presigned-URL для скачивания прикреплённого файла.
 * Делегирует генерацию URL хранилищу через [FileStorage].
 */
interface GetFileDownloadUrlOperation : Operation<GetFileDownloadUrlOperation.Arg, GetFileDownloadUrlOperation.Result> {
    /**
     * Аргумент операции получения URL для скачивания.
     *
     * @property fileId идентификатор прикрепления
     */
    data class Arg(
        val fileId: String,
    )

    /**
     * Результат операции получения URL.
     */
    sealed interface Result {
        /** URL успешно получен. */
        data class Success(
            val url: String,
        ) : Result

        /** Файл не найден. */
        data object NotFound : Result
    }
}
