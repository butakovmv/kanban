package com.kanban.task

/**
 * Репозиторий для доступа к данным комментариев к задачам.
 * Предоставляет методы сохранения, поиска по идентификатору, получения списка по задаче и удаления.
 */
interface CommentRepository {
    /**
     * Сохраняет комментарий (создаёт или обновляет).
     *
     * @param comment сущность комментария
     * @return сохранённый комментарий
     */
    suspend fun save(comment: Comment): Comment

    /**
     * Находит комментарий по идентификатору.
     *
     * @param id идентификатор комментария
     * @return комментарий или null, если не найден
     */
    suspend fun findById(id: String): Comment?

    /**
     * Возвращает список комментариев указанной задачи, упорядоченный по дате создания.
     *
     * @param taskId идентификатор задачи
     * @return список комментариев задачи
     */
    suspend fun listByTaskId(taskId: String): List<Comment>

    /**
     * Удаляет комментарий по идентификатору.
     *
     * @param id идентификатор комментария
     */
    suspend fun delete(id: String)
}
