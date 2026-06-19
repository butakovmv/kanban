package com.kanban.project

/**
 * Репозиторий для доступа к данным досок.
 * Предоставляет методы сохранения, поиска по идентификатору, получения списка по проекту,
 * удаления и архивирования.
 */
interface BoardRepository {
    /**
     * Сохраняет доску (создаёт или обновляет).
     *
     * @param board сущность доски
     * @return сохранённая доска
     */
    suspend fun save(board: Board): Board

    /**
     * Находит доску по идентификатору.
     *
     * @param id идентификатор доски
     * @return доска или null, если не найдена
     */
    suspend fun findById(id: String): Board?

    /**
     * Возвращает список досок указанного проекта, упорядоченный по позиции.
     *
     * @param projectId идентификатор проекта
     * @return список досок проекта
     */
    suspend fun listByProjectId(projectId: String): List<Board>

    /**
     * Удаляет доску по идентификатору.
     *
     * @param id идентификатор доски
     */
    suspend fun delete(id: String)

    /**
     * Архивирует доску (помечает как архивную, скрывает из активных списков).
     *
     * @param id идентификатор доски
     */
    suspend fun archive(id: String)
}
