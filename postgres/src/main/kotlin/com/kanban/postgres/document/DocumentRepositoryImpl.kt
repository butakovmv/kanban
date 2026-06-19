package com.kanban.postgres.document

import com.kanban.document.Document
import com.kanban.document.DocumentRepository
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

/**
 * Реализация [DocumentRepository] через R2DBC и DatabaseClient.
 * Реализует паттерн Upsert: при сохранении проверяет существование документа
 * и выполняет INSERT или UPDATE соответственно.
 */
@Repository
internal class DocumentRepositoryImpl(
    private val db: DatabaseClient,
) : DocumentRepository {
    /**
     * Сохранение документа (создание или обновление).
     * @param document доменная сущность документа
     * @return сохранённый документ
     */
    override suspend fun save(document: Document): Document {
        val z = ZoneId.systemDefault()
        val createdAt = document.createdAt.atZone(z).toLocalDateTime()
        val updatedAt = document.updatedAt.atZone(z).toLocalDateTime()
        if (findById(document.id.value) != null) {
            updateDocument(document, updatedAt)
        } else {
            insertDocument(document, createdAt, updatedAt)
        }
        return document
    }

    /**
     * Обновление существующей записи документа в таблице `documents`.
     * @param document доменная сущность документа с обновлёнными данными
     * @param updatedAt метка времени обновления в часовом поясе системы
     */
    private suspend fun updateDocument(
        document: Document,
        updatedAt: LocalDateTime,
    ) {
        db
            .sql(
                """
                UPDATE documents SET
                    project_id = :projectId, title = :title,
                    description = :description, file_name = :fileName,
                    content_type = :contentType, size_bytes = :sizeBytes,
                    storage_key = :storageKey, version = :version,
                    uploaded_by = :uploadedBy, updated_at = :updatedAt
                WHERE id = :id
                """,
            ).bind("id", document.id.value)
            .bind("projectId", document.projectId.value)
            .bind("title", document.title)
            .let { spec ->
                val description = document.description
                if (description != null) {
                    spec.bind("description", description)
                } else {
                    spec.bindNull("description", String::class.java)
                }
            }.bind("fileName", document.fileName)
            .bind("contentType", document.contentType)
            .bind("sizeBytes", document.sizeBytes)
            .bind("storageKey", document.storageKey)
            .bind("version", document.version)
            .bind("uploadedBy", document.uploadedBy)
            .bind("updatedAt", updatedAt)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    /**
     * Вставка новой записи документа в таблицу `documents`.
     * @param document доменная сущность документа для сохранения
     * @param createdAt метка времени создания
     * @param updatedAt метка времени обновления
     */
    private suspend fun insertDocument(
        document: Document,
        createdAt: LocalDateTime,
        updatedAt: LocalDateTime,
    ) {
        db
            .sql(
                """
                INSERT INTO documents (id, project_id, title, description, file_name,
                    content_type, size_bytes, storage_key, version, uploaded_by,
                    created_at, updated_at)
                VALUES (:id, :projectId, :title, :description, :fileName,
                    :contentType, :sizeBytes, :storageKey, :version, :uploadedBy,
                    :createdAt, :updatedAt)
                """,
            ).bind("id", document.id.value)
            .bind("projectId", document.projectId.value)
            .bind("title", document.title)
            .let { spec ->
                val description = document.description
                if (description != null) {
                    spec.bind("description", description)
                } else {
                    spec.bindNull("description", String::class.java)
                }
            }.bind("fileName", document.fileName)
            .bind("contentType", document.contentType)
            .bind("sizeBytes", document.sizeBytes)
            .bind("storageKey", document.storageKey)
            .bind("version", document.version)
            .bind("uploadedBy", document.uploadedBy)
            .bind("createdAt", createdAt)
            .bind("updatedAt", updatedAt)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    /**
     * Поиск документа по идентификатору.
     * @param id строковый идентификатор документа
     * @return [Document] или null, если документ не найден
     */
    override suspend fun findById(id: String): Document? =
        db
            .sql("SELECT * FROM documents WHERE id = :id")
            .bind("id", id)
            .map { row, _ -> row.toDocument() }
            .one()
            .awaitFirstOrNull()

    /**
     * Получение списка документов указанного проекта, упорядоченных по дате последнего изменения (DESC).
     * @param projectId идентификатор проекта
     * @return список [Document] проекта
     */
    override suspend fun listByProjectId(projectId: String): List<Document> =
        db
            .sql("SELECT * FROM documents WHERE project_id = :projectId ORDER BY updated_at DESC")
            .bind("projectId", projectId)
            .map { row, _ -> row.toDocument() }
            .all()
            .collectList()
            .awaitSingle()

    /**
     * Удаление документа по идентификатору.
     * @param id идентификатор документа
     */
    override suspend fun delete(id: String) {
        db
            .sql("DELETE FROM documents WHERE id = :id")
            .bind("id", id)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    /**
     * Преобразование строки результата запроса R2DBC в доменную сущность [Document].
     * @param row строка результата запроса
     * @return доменная сущность [Document]
     */
    private fun io.r2dbc.spi.Row.toDocument(): Document {
        val table =
            DocumentTable(
                id = get("id", String::class.java)!!,
                projectId = get("project_id", String::class.java)!!,
                title = get("title", String::class.java)!!,
                description = get("description", String::class.java),
                fileName = get("file_name", String::class.java)!!,
                contentType = get("content_type", String::class.java)!!,
                sizeBytes = (get("size_bytes", java.lang.Long::class.java) ?: 0L) as Long,
                storageKey = get("storage_key", String::class.java)!!,
                version = get("version", java.lang.Integer::class.java)!!.toInt(),
                uploadedBy = get("uploaded_by", String::class.java)!!,
                createdAt = get("created_at", java.time.LocalDateTime::class.java)!!,
                updatedAt = get("updated_at", java.time.LocalDateTime::class.java)!!,
            )
        return table.toDomain()
    }
}
