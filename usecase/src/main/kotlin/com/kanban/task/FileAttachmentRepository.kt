package com.kanban.task

/**
 * Репозиторий для доступа к данным прикреплённых к задачам файлов.
 * Хранит только метаданные файлов; содержимое размещается во внешнем хранилище через [FileStorage].
 */
interface FileAttachmentRepository {
    /**
     * Сохраняет прикрепление файла (создаёт или обновляет).
     *
     * @param file сущность прикрепления
     * @return сохранённое прикрепление
     */
    suspend fun save(file: FileAttachment): FileAttachment

    /**
     * Находит прикрепление файла по идентификатору.
     *
     * @param id идентификатор прикрепления
     * @return прикрепление или null, если не найдено
     */
    suspend fun findById(id: String): FileAttachment?

    /**
     * Возвращает список прикреплений файлов указанной задачи.
     *
     * @param taskId идентификатор задачи
     * @return список прикреплений задачи
     */
    suspend fun listByTaskId(taskId: String): List<FileAttachment>

    /**
     * Удаляет прикрепление файла по идентификатору.
     *
     * @param id идентификатор прикрепления
     */
    suspend fun delete(id: String)
}
