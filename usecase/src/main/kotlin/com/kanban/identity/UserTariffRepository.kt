package com.kanban.identity

/**
 * Репозиторий для доступа к данным о тарифах пользователей.
 * Предоставляет методы поиска активного тарифа и сохранения назначения тарифа.
 */
interface UserTariffRepository {
    /**
     * Находит активный тариф пользователя (текущий или будущий).
     *
     * @param userId идентификатор пользователя
     * @return активный тариф пользователя или null
     */
    suspend fun findActiveByUserId(userId: String): UserTariff?

    /**
     * Сохраняет назначение тарифа пользователю.
     *
     * @param userTariff сущность связи пользователя и тарифа
     * @return сохранённая сущность
     */
    suspend fun save(userTariff: UserTariff): UserTariff
}
