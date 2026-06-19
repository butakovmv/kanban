package com.kanban.identity

/**
 * Репозиторий для доступа к данным тарифов.
 * Предоставляет методы поиска по идентификатору, имени и получения списка всех тарифов.
 */
interface TariffRepository {
    /**
     * Находит тариф по идентификатору.
     *
     * @param tariffId идентификатор тарифа
     * @return тариф или null, если не найден
     */
    suspend fun findById(tariffId: String): Tariff?

    /**
     * Находит тариф по имени.
     *
     * @param name название тарифа
     * @return тариф или null, если не найден
     */
    suspend fun findByName(name: String): Tariff?

    /**
     * Возвращает список всех тарифов.
     *
     * @return список тарифов
     */
    suspend fun listAll(): List<Tariff>
}
