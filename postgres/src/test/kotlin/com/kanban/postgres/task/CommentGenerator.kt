package com.kanban.postgres.task

import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient

internal class CommentGenerator(
    private val db: DatabaseClient,
) {
    suspend fun createAndInsert(
        taskId: String,
        authorId: String = UUID.randomUUID().toString(),
        text: String = "Comment-${UUID.randomUUID().toString().take(8)}",
    ): String {
        val id = UUID.randomUUID().toString()
        val now = LocalDateTime.now()
        db
            .sql(
                """
                INSERT INTO comments (id, task_id, author_id, text, created_at, updated_at)
                VALUES (:id, :taskId, :authorId, :text, :createdAt, :updatedAt)
                """,
            ).bind("id", id)
            .bind("taskId", taskId)
            .bind("authorId", authorId)
            .bind("text", text)
            .bind("createdAt", now)
            .bind("updatedAt", now)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
        return id
    }

    suspend fun deleteAll() {
        db
            .sql("DELETE FROM comments")
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
