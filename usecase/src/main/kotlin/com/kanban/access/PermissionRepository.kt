package com.kanban.access

/**
 * Репозиторий для доступа к данным разрешений на ресурсы.
 * Предоставляет методы сохранения, поиска по идентификатору, выборки по ресурсу/цели и удаления.
 */
interface PermissionRepository {
    /**
     * Сохраняет разрешение (создаёт или обновляет).
     *
     * @param permission сущность разрешения
     * @return сохранённое разрешение
     */
    suspend fun save(permission: Permission): Permission

    /**
     * Находит разрешение по идентификатору.
     *
     * @param id идентификатор разрешения
     * @return разрешение или null, если не найдено
     */
    suspend fun findById(id: String): Permission?

    /**
     * Возвращает разрешения, относящиеся к указанному ресурсу и (опционально) к конкретному экземпляру.
     * Если [targetId] равен null — возвращаются все разрешения для ресурса (включая глобальные и адресные).
     *
     * @param resource тип ресурса
     * @param targetId идентификатор экземпляра ресурса или null для выборки по типу ресурса
     * @return список подходящих разрешений
     */
    suspend fun findByResource(
        resource: String,
        targetId: String?,
    ): List<Permission>

    /**
     * Удаляет разрешение по идентификатору.
     *
     * @param id идентификатор разрешения
     */
    suspend fun delete(id: String)
}
