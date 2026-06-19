package com.kanban.document

/**
 * Репозиторий для доступа к данным документов.
 * Хранит только метаданные документов; содержимое размещается во внешнем хранилище через [DocumentStorage].
 */
interface DocumentRepository {
    /**
     * Сохраняет документ (создаёт или обновляет).
     *
     * @param document сущность документа
     * @return сохранённый документ
     */
    suspend fun save(document: Document): Document

    /**
     * Находит документ по идентификатору.
     *
     * @param id идентификатор документа
     * @return документ или null, если не найден
     */
    suspend fun findById(id: String): Document?

    /**
     * Возвращает список документов указанного проекта, упорядоченный по дате последнего изменения (DESC).
     *
     * @param projectId идентификатор проекта
     * @return список документов проекта
     */
    suspend fun listByProjectId(projectId: String): List<Document>

    /**
     * Удаляет документ по идентификатору.
     *
     * @param id идентификатор документа
     */
    suspend fun delete(id: String)
}
