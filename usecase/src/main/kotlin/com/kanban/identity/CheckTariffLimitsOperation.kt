package com.kanban.identity

import com.kanban.common.Operation

/**
 * Операция проверки лимитов тарифа пользователя перед созданием ресурса.
 * Определяет, может ли пользователь создать проект, доску или задачу в рамках текущего тарифа.
 */
interface CheckTariffLimitsOperation : Operation<CheckTariffLimitsOperation.Arg, CheckTariffLimitsOperation.Result> {
    /**
     * Аргумент операции проверки лимитов.
     *
     * @property userId идентификатор пользователя
     * @property resourceType тип ресурса (PROJECT, BOARD, TASK)
     * @property requestedCount запрашиваемое количество ресурсов (по умолчанию 1)
     */
    data class Arg(
        val userId: String,
        val resourceType: ResourceType,
        val requestedCount: Int = 1,
    )

    /**
     * Тип ресурса, для которого проверяется лимит тарифа.
     */
    enum class ResourceType {
        PROJECT,
        BOARD,
        TASK,
    }

    /**
     * Результат проверки лимитов тарифа.
     */
    sealed interface Result {
        /** Лимит не превышен, действие разрешено. */
        data object Allowed : Result

        /** Лимит превышен, действие запрещено с указанием причины. */
        data class Denied(
            val reason: String,
        ) : Result
    }
}
