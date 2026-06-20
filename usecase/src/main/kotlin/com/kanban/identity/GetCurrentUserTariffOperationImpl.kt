package com.kanban.identity

internal class GetCurrentUserTariffOperationImpl(
    private val userTariffRepository: UserTariffRepository,
    private val tariffRepository: TariffRepository,
) : GetCurrentUserTariffOperation {
    override suspend fun execute(arg: GetCurrentUserTariffOperation.Arg): GetCurrentUserTariffOperation.Result {
        val userTariff =
            userTariffRepository.findActiveByUserId(arg.userId)
                ?: return GetCurrentUserTariffOperation.Result.NotFound
        val tariff =
            tariffRepository.findById(userTariff.tariffId)
                ?: return GetCurrentUserTariffOperation.Result.NotFound
        return GetCurrentUserTariffOperation.Result.Success(
            TariffInfo(
                name = tariff.name,
                maxProjects = tariff.limits.maxProjects,
                maxBoardsPerProject = tariff.limits.maxBoardsPerProject,
                maxTasksPerBoard = tariff.limits.maxTasksPerBoard,
                maxFileSizeMb = tariff.limits.maxFileSizeMb,
                maxStorageMb = tariff.limits.maxStorageMb,
            ),
        )
    }
}
