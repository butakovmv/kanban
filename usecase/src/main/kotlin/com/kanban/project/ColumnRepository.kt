package com.kanban.project

/**
 * Репозиторий для доступа к данным колонок доски.
 * Предоставляет методы сохранения, поиска по идентификатору, получения списка по доске,
 * удаления и массового обновления позиций (для реордеринга).
 */
interface ColumnRepository {
    /**
     * Сохраняет колонку (создаёт или обновляет).
     *
     * @param column сущность колонки
     * @return сохранённая колонка
     */
    suspend fun save(column: Column): Column

    /**
     * Находит колонку по идентификатору.
     *
     * @param id идентификатор колонки
     * @return колонка или null, если не найдена
     */
    suspend fun findById(id: String): Column?

    /**
     * Возвращает список колонок указанной доски, упорядоченный по позиции.
     *
     * @param boardId идентификатор доски
     * @return список колонок доски
     */
    suspend fun listByBoardId(boardId: String): List<Column>

    /**
     * Удаляет колонку по идентификатору.
     *
     * @param id идентификатор колонки
     */
    suspend fun delete(id: String)

    /**
     * Массово обновляет позиции колонок (для реордеринга).
     * Позиции рассчитываются по порядку элементов в переданном списке.
     *
     * @param columns колонки с обновлёнными позициями
     */
    suspend fun updatePositions(columns: List<Column>)
}
