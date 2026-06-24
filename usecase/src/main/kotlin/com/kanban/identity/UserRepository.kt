package com.kanban.identity

/**
 * Репозиторий для доступа к данным пользователей.
 * Предоставляет методы сохранения, поиска по ID и email, а также проверки существования.
 */
interface UserRepository {
    /**
     * Сохраняет пользователя (создаёт или обновляет).
     *
     * @param user сущность пользователя
     * @return сохранённый пользователь
     */
    suspend fun save(user: User): User

    /**
     * Находит пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return пользователь или null, если не найден
     */
    suspend fun findById(userId: String): User?

    /**
     * Находит пользователя по email.
     *
     * @param email email-адрес
     * @return пользователь или null, если не найден
     */
    suspend fun findByEmail(email: String): User?

    /**
     * Проверяет, существует ли пользователь с указанным email.
     *
     * @param email email-адрес
     * @return true, если пользователь существует
     */
    suspend fun existsByEmail(email: String): Boolean

    /**
     * Находит пользователей по списку идентификаторов.
     *
     * @param userIds список идентификаторов пользователей
     * @return список найденных пользователей
     */
    suspend fun findByIds(userIds: List<String>): List<User>
}
