package com.kanban.access

import com.kanban.common.Operation

/**
 * Операция удаления группы.
 * Каскадно удаляет все членства и связи группы с разрешениями.
 */
interface DeleteGroupOperation : Operation<DeleteGroupOperation.Arg, DeleteGroupOperation.Result> {
    /**
     * Аргумент операции удаления группы.
     *
     * @property groupId идентификатор группы
     */
    data class Arg(
        val groupId: String,
    )

    /**
     * Результат операции удаления группы.
     */
    sealed interface Result {
        /** Группа и все связанные сущности успешно удалены. */
        data object Success : Result

        /** Группа не найдена. */
        data object NotFound : Result
    }
}
