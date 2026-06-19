package com.kanban.task

import com.kanban.common.Operation

/**
 * Операция получения списка прикреплённых к задаче файлов.
 */
interface ListFilesOperation : Operation<ListFilesOperation.Arg, ListFilesOperation.Result> {
    /**
     * Аргумент операции получения списка файлов.
     *
     * @property taskId идентификатор задачи
     */
    data class Arg(
        val taskId: String,
    )

    /**
     * Результат операции получения списка файлов.
     */
    sealed interface Result {
        /** Список файлов успешно получен. */
        data class Success(
            val files: List<FileAttachment>,
        ) : Result
    }
}
