package com.kanban.postgres.project

import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient

internal class ColumnGenerator(
    private val db: DatabaseClient,
) {
    suspend fun createAndInsert(
        boardId: String,
        name: String = "Column-${UUID.randomUUID().toString().take(8)}",
        position: Int = 0,
        wipLimit: Int? = null,
    ): String {
        val id = UUID.randomUUID().toString()
        val spec =
            db
                .sql(
                    """
                    INSERT INTO columns (id, board_id, name, position, wip_limit, created_at)
                    VALUES (:id, :boardId, :name, :position, :wipLimit, :createdAt)
                    """,
                ).bind("id", id)
                .bind("boardId", boardId)
                .bind("name", name)
                .bind("position", position)
                .bind("createdAt", LocalDateTime.now())
        if (wipLimit != null) {
            spec
                .bind("wipLimit", wipLimit)
                .fetch()
                .rowsUpdated()
                .awaitSingle()
        } else {
            spec
                .bindNull("wipLimit", Integer::class.java)
                .fetch()
                .rowsUpdated()
                .awaitSingle()
        }
        return id
    }

    suspend fun deleteAll() {
        db
            .sql("DELETE FROM columns")
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
