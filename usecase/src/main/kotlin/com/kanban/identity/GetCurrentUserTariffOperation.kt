package com.kanban.identity

import com.kanban.common.Operation

data class TariffInfo(
    val name: String,
    val maxProjects: Int,
    val maxBoardsPerProject: Int,
    val maxTasksPerBoard: Int,
    val maxFileSizeMb: Int,
    val maxStorageMb: Int,
)

@Suppress("MaxLineLength")
interface GetCurrentUserTariffOperation : Operation<GetCurrentUserTariffOperation.Arg, GetCurrentUserTariffOperation.Result> {
    data class Arg(
        val userId: String,
    )

    sealed class Result {
        data class Success(
            val tariff: TariffInfo,
        ) : Result()

        data object NotFound : Result()
    }
}
