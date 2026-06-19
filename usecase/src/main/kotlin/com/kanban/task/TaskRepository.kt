package com.kanban.task

/**
 * Репозиторий для доступа к данным задач.
 * Предоставляет методы сохранения, поиска по идентификатору, получения списка по доске/колонке,
 * удаления, массового обновления позиций (для реордеринга) и архивирования.
 */
interface TaskRepository {
    /**
     * Сохраняет задачу (создаёт или обновляет).
     *
     * @param task сущность задачи
     * @return сохранённая задача
     */
    suspend fun save(task: Task): Task

    /**
     * Находит задачу по идентификатору.
     *
     * @param id идентификатор задачи
     * @return задача или null, если не найдена
     */
    suspend fun findById(id: String): Task?

    /**
     * Возвращает список задач указанной доски, упорядоченный по позиции.
     *
     * @param boardId идентификатор доски
     * @param includeArchived включать ли архивные задачи в результат
     * @return список задач доски
     */
    suspend fun listByBoardId(
        boardId: String,
        includeArchived: Boolean = false,
    ): List<Task>

    /**
     * Возвращает список задач указанной колонки, упорядоченный по позиции.
     *
     * @param columnId идентификатор колонки
     * @return список задач колонки
     */
    suspend fun listByColumnId(columnId: String): List<Task>

    /**
     * Удаляет задачу по идентификатору.
     *
     * @param id идентификатор задачи
     */
    suspend fun delete(id: String)

    /**
     * Массово обновляет позиции задач (для реордеринга или перемещения).
     * Позиции рассчитываются по порядку элементов в переданном списке.
     *
     * @param tasks задачи с обновлёнными позициями и/или колонками
     */
    suspend fun updatePositions(tasks: List<Task>)

    /**
     * Архивирует задачу (помечает как архивную, скрывает из активных списков).
     *
     * @param id идентификатор задачи
     */
    suspend fun archive(id: String)
}
