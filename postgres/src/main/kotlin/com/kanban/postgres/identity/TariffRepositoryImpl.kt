package com.kanban.postgres.identity

import com.kanban.identity.Tariff
import com.kanban.identity.TariffRepository
import java.util.UUID
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

/**
 * Реализация [TariffRepository] через R2DBC и DatabaseClient.
 * Предоставляет методы поиска тарифов по идентификатору, имени и получения полного списка.
 */
@Repository
internal class TariffRepositoryImpl(
    private val db: DatabaseClient,
) : TariffRepository {
    /**
     * Поиск тарифа по идентификатору.
     * @param tariffId строковый идентификатор тарифа
     * @return [Tariff] или null, если тариф не найден
     */
    override suspend fun findById(tariffId: String): Tariff? =
        db
            .sql("SELECT * FROM tariffs WHERE id = :id")
            .bind("id", UUID.fromString(tariffId))
            .map { row, _ -> row.toTariff() }
            .one()
            .awaitFirstOrNull()

    /**
     * Поиск тарифа по названию.
     * @param name название тарифа
     * @return [Tariff] или null, если тариф не найден
     */
    override suspend fun findByName(name: String): Tariff? =
        db
            .sql("SELECT * FROM tariffs WHERE name = :name")
            .bind("name", name)
            .map { row, _ -> row.toTariff() }
            .one()
            .awaitFirstOrNull()

    /**
     * Получение списка всех тарифов, отсортированных по названию.
     * @return список всех [Tariff]
     */
    override suspend fun listAll(): List<Tariff> =
        db
            .sql("SELECT * FROM tariffs ORDER BY name")
            .map { row, _ -> row.toTariff() }
            .all()
            .collectList()
            .awaitSingle()

    override suspend fun save(tariff: Tariff): Tariff {
        val table = tariff.toTable()
        db
            .sql(
                """
            INSERT INTO tariffs (id, name, max_projects, max_boards_per_project, max_tasks_per_board,
                                 max_file_size_mb, max_storage_mb, created_at)
            VALUES (:id, :name, :maxProjects, :maxBoardsPerProject, :maxTasksPerBoard,
                    :maxFileSizeMb, :maxStorageMb, :createdAt)
            ON CONFLICT (id) DO UPDATE SET
                name = EXCLUDED.name,
                max_projects = EXCLUDED.max_projects,
                max_boards_per_project = EXCLUDED.max_boards_per_project,
                max_tasks_per_board = EXCLUDED.max_tasks_per_board,
                max_file_size_mb = EXCLUDED.max_file_size_mb,
                max_storage_mb = EXCLUDED.max_storage_mb
            """,
            ).bind("id", UUID.fromString(table.id))
            .bind("name", table.name)
            .bind("maxProjects", table.maxProjects)
            .bind("maxBoardsPerProject", table.maxBoardsPerProject)
            .bind("maxTasksPerBoard", table.maxTasksPerBoard)
            .bind("maxFileSizeMb", table.maxFileSizeMb)
            .bind("maxStorageMb", table.maxStorageMb)
            .bind("createdAt", table.createdAt)
            .then()
            .awaitFirstOrNull()
        return tariff
    }

    /**
     * Преобразование строки результата запроса R2DBC в доменную сущность [Tariff].
     * Считывает колонки таблицы `tariffs` и создаёт [TariffTable], затем маппит в домен.
     * @param row строка результата запроса
     * @return доменная сущность [Tariff]
     */
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
