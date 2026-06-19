package com.kanban.project

/**
 * Репозиторий для доступа к данным проектов.
 * Предоставляет методы сохранения, поиска по идентификатору, получения списка по владельцу и удаления.
 */
interface ProjectRepository {
    /**
     * Сохраняет проект (создаёт или обновляет).
     *
     * @param project сущность проекта
     * @return сохранённый проект
     */
    suspend fun save(project: Project): Project

    /**
     * Находит проект по идентификатору.
     *
     * @param id идентификатор проекта
     * @return проект или null, если не найден
     */
    suspend fun findById(id: String): Project?

    /**
     * Возвращает список проектов указанного владельца.
     *
     * @param ownerId идентификатор пользователя-владельца
     * @return список проектов владельца
     */
    suspend fun listByOwnerId(ownerId: String): List<Project>

    /**
     * Удаляет проект по идентификатору.
     *
     * @param id идентификатор проекта
     */
    suspend fun delete(id: String)
}
