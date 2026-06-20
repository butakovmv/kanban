package com.kanban.access

/**
 * Репозиторий для управления связями «группа-разрешение» и проверки доступа пользователей.
 */
interface GroupPermissionRepository {
    /**
     * Выдаёт указанное разрешение группе.
     *
     * @param groupId идентификатор группы
     * @param permissionId идентификатор разрешения
     */
    suspend fun grant(
        groupId: String,
        permissionId: String,
    )

    /**
     * Отзывает указанное разрешение у группы.
     *
     * @param groupId идентификатор группы
     * @param permissionId идентификатор разрешения
     */
    suspend fun revoke(
        groupId: String,
        permissionId: String,
    )

    /**
     * Возвращает список разрешений, назначенных указанной группе.
     *
     * @param groupId идентификатор группы
     * @return список разрешений группы
     */
    suspend fun listPermissionsForGroup(groupId: String): List<Permission>

    /**
     * Возвращает список всех разрешений, доступных пользователю через группы, в которых он состоит.
     *
     * @param userId идентификатор пользователя
     * @return список разрешений пользователя
     */
    suspend fun listPermissionsForUser(userId: String): List<Permission>

    /**
     * Удаляет все связи группы с разрешениями.
     * Используется при каскадном удалении группы.
     *
     * @param groupId идентификатор группы
     */
    suspend fun deleteAllByGroup(groupId: String)
}
