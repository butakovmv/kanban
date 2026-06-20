package com.kanban.identity

internal class ProfileHandler(
    private val getCurrentUserTariffOperation: GetCurrentUserTariffOperation,
) {
    suspend fun getTariff(userId: String): TariffResult {
        val request = GetCurrentUserTariffOperation.Arg(userId = userId)
        return when (val result = getCurrentUserTariffOperation.execute(request)) {
            is GetCurrentUserTariffOperation.Result.Success -> TariffResult.Success(result.tariff)
            is GetCurrentUserTariffOperation.Result.NotFound -> TariffResult.NotFound
        }
    }
}

sealed class TariffResult {
    data class Success(
        val tariff: TariffInfo,
    ) : TariffResult()

    data object NotFound : TariffResult()
}
