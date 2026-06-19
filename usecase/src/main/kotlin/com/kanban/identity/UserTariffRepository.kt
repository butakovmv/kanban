package com.kanban.identity

interface UserTariffRepository {
    suspend fun findActiveByUserId(userId: String): UserTariff?

    suspend fun save(userTariff: UserTariff): UserTariff
}
