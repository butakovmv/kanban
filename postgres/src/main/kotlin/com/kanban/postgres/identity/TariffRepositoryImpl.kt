package com.kanban.postgres.identity

import com.kanban.identity.Tariff
import com.kanban.identity.TariffRepository
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
internal class TariffRepositoryImpl(
    private val db: DatabaseClient,
) : TariffRepository {
    override suspend fun findById(tariffId: String): Tariff? =
        db
            .sql("SELECT * FROM tariffs WHERE id = :id")
            .bind("id", tariffId)
            .map { row, _ -> row.toTariff() }
            .one()
            .awaitFirstOrNull()

    override suspend fun findByName(name: String): Tariff? =
        db
            .sql("SELECT * FROM tariffs WHERE name = :name")
            .bind("name", name)
            .map { row, _ -> row.toTariff() }
            .one()
            .awaitFirstOrNull()

    override suspend fun listAll(): List<Tariff> =
        db
            .sql("SELECT * FROM tariffs ORDER BY name")
            .map { row, _ -> row.toTariff() }
            .all()
            .collectList()
            .awaitSingle()

    private fun io.r2dbc.spi.Row.toTariff(): Tariff {
        val table =
            TariffTable(
                id = get("id", String::class.java)!!,
                name = get("name", String::class.java)!!,
                maxProjects = get("max_projects", java.lang.Integer::class.java)!!.toInt(),
                maxBoardsPerProject = get("max_boards_per_project", java.lang.Integer::class.java)!!.toInt(),
                maxTasksPerBoard = get("max_tasks_per_board", java.lang.Integer::class.java)!!.toInt(),
                maxFileSizeMb = get("max_file_size_mb", java.lang.Integer::class.java)!!.toInt(),
                maxStorageMb = get("max_storage_mb", java.lang.Integer::class.java)!!.toInt(),
                createdAt = get("created_at", java.time.LocalDateTime::class.java)!!,
            )
        return table.toDomain()
    }
}
