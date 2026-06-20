package com.kanban.access

/**
 * Репозиторий для доступа к данным групп пользователей.
 * Предоставляет методы сохранения, поиска по идентификатору, получения полного списка и удаления.
 */
interface GroupRepository {
    /**
     * Сохраняет группу (создаёт или обновляет).
     *
     * @param group сущность группы
     * @return сохранённая группа
     */
    suspend fun save(group: Group): Group

    /**
     * Находит группу по идентификатору.
     *
     * @param id идентификатор группы
     * @return группа или null, если не найдена
     */
    suspend fun findById(id: String): Group?

    /**
     * Возвращает полный список групп.
     *
     * @return список всех групп
     */
    suspend fun listAll(): List<Group>

    /**
     * Удаляет группу по идентификатору.
     *
     * @param id идентификатор группы
     */
    suspend fun delete(id: String)
}
