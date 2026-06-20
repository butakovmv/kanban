package com.kanban.access

import com.kanban.common.Operation

/**
 * Операция получения группы по идентификатору.
 */
interface GetGroupOperation : Operation<GetGroupOperation.Arg, GetGroupOperation.Result> {
    /**
     * Аргумент операции получения группы.
     *
     * @property groupId идентификатор группы
     */
    data class Arg(
        val groupId: String,
    )

    /**
     * Результат операции получения группы.
     */
    sealed interface Result {
        /** Группа найдена. */
        data class Success(
            val group: Group,
        ) : Result

        /** Группа не найдена. */
        data object NotFound : Result
    }
}
