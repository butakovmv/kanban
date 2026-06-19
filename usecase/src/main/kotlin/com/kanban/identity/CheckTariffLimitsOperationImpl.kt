package com.kanban.identity

/**
 * Реализация операции проверки лимитов тарифа.
 * Находит активный тариф пользователя и сверяет запрошенное количество ресурсов с лимитом.
 */
internal class CheckTariffLimitsOperationImpl(
    private val userTariffRepository: UserTariffRepository,
    private val tariffRepository: TariffRepository,
) : CheckTariffLimitsOperation {
    override suspend fun execute(arg: CheckTariffLimitsOperation.Arg): CheckTariffLimitsOperation.Result {
        val userTariff =
            userTariffRepository.findActiveByUserId(arg.userId)
                ?: return CheckTariffLimitsOperation.Result.Denied("No active tariff")

        val tariff =
            tariffRepository.findById(userTariff.tariffId)
                ?: return CheckTariffLimitsOperation.Result.Denied("Tariff not found")

        val limit =
            when (arg.resourceType) {
                CheckTariffLimitsOperation.ResourceType.PROJECT -> tariff.limits.maxProjects
                CheckTariffLimitsOperation.ResourceType.BOARD -> tariff.limits.maxBoardsPerProject
                CheckTariffLimitsOperation.ResourceType.TASK -> tariff.limits.maxTasksPerBoard
            }

        return if (arg.requestedCount > limit) {
            CheckTariffLimitsOperation.Result.Denied(
                "${
                    arg.resourceType.name.lowercase().replaceFirstChar { it.uppercase() }
                } limit exceeded: max $limit",
            )
        } else {
            CheckTariffLimitsOperation.Result.Allowed
        }
    }
}
