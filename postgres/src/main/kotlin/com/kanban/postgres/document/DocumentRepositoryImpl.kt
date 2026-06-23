package com.kanban.postgres.document

import com.kanban.document.Document
import com.kanban.document.DocumentRepository
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
internal class DocumentRepositoryImpl(
    private val db: DatabaseClient,
) : DocumentRepository {
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

    private suspend fun updateDocument(
        document: Document,
        updatedAt: LocalDateTime,
    ) {
        db
            .sql(
                """
                UPDATE documents SET
                    project_id = :projectId, path = :path, title = :title,
                    content = :content, description = :description, updated_at = :updatedAt
                WHERE id = :id
                """,
            ).bind("id", UUID.fromString(document.id.value))
            .bind("projectId", UUID.fromString(document.projectId.value))
            .bind("path", document.path)
            .bind("title", document.title)
            .bind("content", document.content)
            .let { spec ->
                val description = document.description
                if (description != null) {
                    spec.bind("description", description)
                } else {
                    spec.bindNull("description", String::class.java)
                }
            }.bind("updatedAt", updatedAt)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    private suspend fun insertDocument(
        document: Document,
        createdAt: LocalDateTime,
        updatedAt: LocalDateTime,
    ) {
        db
            .sql(
                """
                INSERT INTO documents (id, project_id, path, title, content, description, created_at, updated_at)
                VALUES (:id, :projectId, :path, :title, :content, :description, :createdAt, :updatedAt)
                """,
            ).bind("id", UUID.fromString(document.id.value))
            .bind("projectId", UUID.fromString(document.projectId.value))
            .bind("path", document.path)
            .bind("title", document.title)
            .bind("content", document.content)
            .let { spec ->
                val description = document.description
                if (description != null) {
                    spec.bind("description", description)
                } else {
                    spec.bindNull("description", String::class.java)
                }
            }.bind("createdAt", createdAt)
            .bind("updatedAt", updatedAt)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    override suspend fun findById(id: String): Document? =
        db
            .sql("SELECT * FROM documents WHERE id = :id")
            .bind("id", UUID.fromString(id))
            .map { row, _ -> row.toDocument() }
            .one()
            .awaitFirstOrNull()

    override suspend fun listByProjectId(projectId: String): List<Document> =
        db
            .sql("SELECT * FROM documents WHERE project_id = :projectId ORDER BY path ASC")
            .bind("projectId", UUID.fromString(projectId))
            .map { row, _ -> row.toDocument() }
            .all()
            .collectList()
            .awaitSingle()

    override suspend fun delete(id: String) {
        db
            .sql("DELETE FROM documents WHERE id = :id")
            .bind("id", UUID.fromString(id))
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    private fun io.r2dbc.spi.Row.toDocument(): Document {
        val table =
            DocumentTable(
                id = get("id", String::class.java)!!,
                projectId = get("project_id", String::class.java)!!,
                path = get("path", String::class.java)!!,
                title = get("title", String::class.java)!!,
                content = get("content", String::class.java)!!,
                description = get("description", String::class.java),
                createdAt = get("created_at", java.time.LocalDateTime::class.java)!!,
                updatedAt = get("updated_at", java.time.LocalDateTime::class.java)!!,
            )
        return table.toDomain()
    }
}
