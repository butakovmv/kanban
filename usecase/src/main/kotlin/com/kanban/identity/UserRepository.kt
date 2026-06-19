package com.kanban.identity

interface UserRepository {
    suspend fun save(user: User): User

    suspend fun findById(userId: String): User?

    suspend fun findByEmail(email: String): User?

    suspend fun existsByEmail(email: String): Boolean
}
