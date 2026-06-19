package com.kanban.identity

interface TariffRepository {
    suspend fun findById(tariffId: String): Tariff?

    suspend fun findByName(name: String): Tariff?

    suspend fun listAll(): List<Tariff>
}
