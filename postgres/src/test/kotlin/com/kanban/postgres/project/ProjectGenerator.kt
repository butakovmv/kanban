package com.kanban.postgres.project

import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient

internal class ProjectGenerator(
    private val db: DatabaseClient,
) {
    suspend fun createAndInsert(
        ownerId: String = UUID.randomUUID().toString(),
        name: String = "Project-${UUID.randomUUID().toString().take(8)}",
        description: String? = null,
    ): String {
        val id = UUID.randomUUID().toString()
        val now = LocalDateTime.now()
        val spec =
            db
                .sql(
                    """
                    INSERT INTO projects (id, owner_id, name, description, created_at, updated_at)
                    VALUES (:id, :ownerId, :name, :description, :createdAt, :updatedAt)
                    """,
                ).bind("id", id)
                .bind("ownerId", ownerId)
                .bind("name", name)
                .bind("createdAt", now)
                .bind("updatedAt", now)
        if (description != null) {
            spec
                .bind("description", description)
                .fetch()
                .rowsUpdated()
                .awaitSingle()
        } else {
            spec
                .bindNull("description", String::class.java)
                .fetch()
                .rowsUpdated()
                .awaitSingle()
        }
        return id
    }

    suspend fun deleteAll() {
        db
            .sql("DELETE FROM projects")
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
