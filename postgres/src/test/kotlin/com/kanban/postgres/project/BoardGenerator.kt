package com.kanban.postgres.project

import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient

internal class BoardGenerator(
    private val db: DatabaseClient,
) {
    suspend fun createAndInsert(
        projectId: String,
        name: String = "Board-${UUID.randomUUID().toString().take(8)}",
        position: Int = 0,
        archived: Boolean = false,
    ): String {
        val id = UUID.randomUUID().toString()
        db
            .sql(
                """
                INSERT INTO boards (id, project_id, name, position, archived, created_at)
                VALUES (:id, :projectId, :name, :position, :archived, :createdAt)
                """,
            ).bind("id", id)
            .bind("projectId", projectId)
            .bind("name", name)
            .bind("position", position)
            .bind("archived", archived)
            .bind("createdAt", LocalDateTime.now())
            .fetch()
            .rowsUpdated()
            .awaitSingle()
        return id
    }

    suspend fun deleteAll() {
        db
            .sql("DELETE FROM boards")
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
