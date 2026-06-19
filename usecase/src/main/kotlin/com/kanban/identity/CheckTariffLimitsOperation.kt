package com.kanban.identity

import com.kanban.common.Operation

interface CheckTariffLimitsOperation : Operation<CheckTariffLimitsOperation.Arg, CheckTariffLimitsOperation.Result> {
    data class Arg(
        val userId: String,
        val resourceType: ResourceType,
        val requestedCount: Int = 1,
    )

    enum class ResourceType {
        PROJECT,
        BOARD,
        TASK,
    }

    sealed interface Result {
        data object Allowed : Result

        data class Denied(
            val reason: String,
        ) : Result
    }
}
