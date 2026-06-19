package com.kanban.postgres.identity

import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient

internal data class TariffParams(
    val name: String = "Tariff-${UUID.randomUUID().toString().take(8)}",
    val maxProjects: Int = 5,
    val maxBoards: Int = 10,
    val maxTasks: Int = 50,
    val maxFileSize: Int = 10,
    val maxStorage: Int = 500,
)

internal class TariffGenerator(
    private val db: DatabaseClient,
) {
    suspend fun createAndInsert(params: TariffParams = TariffParams()): String {
        val id = UUID.randomUUID().toString()
        db
            .sql(
                """
            INSERT INTO tariffs (id, name, max_projects, max_boards_per_project, max_tasks_per_board, max_file_size_mb, max_storage_mb, created_at)
            VALUES (:id, :name, :maxProjects, :maxBoards, :maxTasks, :maxFileSize, :maxStorage, :createdAt)
        """,
            ).bind("id", id)
            .bind("name", params.name)
            .bind("maxProjects", params.maxProjects)
            .bind("maxBoards", params.maxBoards)
            .bind("maxTasks", params.maxTasks)
            .bind("maxFileSize", params.maxFileSize)
            .bind("maxStorage", params.maxStorage)
            .bind("createdAt", LocalDateTime.now())
            .fetch()
            .rowsUpdated()
            .awaitSingle()
        return id
    }

    suspend fun deleteAll() {
        db
            .sql("DELETE FROM tariffs")
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
