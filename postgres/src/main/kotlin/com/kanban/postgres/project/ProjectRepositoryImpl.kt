package com.kanban.postgres.project

import com.kanban.project.Project
import com.kanban.project.ProjectRepository
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

/**
 * Реализация [ProjectRepository] через R2DBC и DatabaseClient.
 * Реализует паттерн Upsert: при сохранении проверяет существование проекта
 * и выполняет INSERT или UPDATE соответственно.
 */
@Repository
internal class ProjectRepositoryImpl(
    private val db: DatabaseClient,
) : ProjectRepository {
    /**
     * Сохранение проекта (создание или обновление).
     * @param project доменная сущность проекта
     * @return сохранённый проект
     */
    override suspend fun save(project: Project): Project {
        val z = ZoneId.systemDefault()
        val createdAt = project.createdAt.atZone(z).toLocalDateTime()
        val updatedAt = project.updatedAt.atZone(z).toLocalDateTime()
        if (findById(project.id.value) != null) {
            updateProject(project, updatedAt)
        } else {
            insertProject(project, createdAt, updatedAt)
        }
        return project
    }

    /**
     * Обновление существующей записи проекта в таблице `projects`.
     * @param project доменная сущность проекта с обновлёнными данными
     * @param updatedAt метка времени обновления в часовом поясе системы
     */
    private suspend fun updateProject(
        project: Project,
        updatedAt: LocalDateTime,
    ) {
        db
            .sql(
                """
                UPDATE projects SET
                    owner_id = :ownerId, name = :name,
                    description = :description, updated_at = :updatedAt
                WHERE id = :id
                """,
            ).bind("id", project.id.value)
            .bind("ownerId", project.ownerId.value)
            .bind("name", project.name)
            .let { spec ->
                val description = project.description
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

    /**
     * Вставка новой записи проекта в таблицу `projects`.
     * @param project доменная сущность проекта для сохранения
     * @param createdAt метка времени создания
     * @param updatedAt метка времени обновления
     */
    private suspend fun insertProject(
        project: Project,
        createdAt: LocalDateTime,
        updatedAt: LocalDateTime,
    ) {
        db
            .sql(
                """
                INSERT INTO projects (id, owner_id, name, description, created_at, updated_at)
                VALUES (:id, :ownerId, :name, :description, :createdAt, :updatedAt)
                """,
            ).bind("id", project.id.value)
            .bind("ownerId", project.ownerId.value)
            .bind("name", project.name)
            .let { spec ->
                val description = project.description
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

    /**
     * Поиск проекта по идентификатору.
     * @param id строковый идентификатор проекта
     * @return [Project] или null, если проект не найден
     */
    override suspend fun findById(id: String): Project? =
        db
            .sql("SELECT * FROM projects WHERE id = :id")
            .bind("id", id)
            .map { row, _ -> row.toProject() }
            .one()
            .awaitFirstOrNull()

    /**
     * Получение списка проектов указанного владельца, упорядоченных по дате создания.
     * @param ownerId идентификатор пользователя-владельца
     * @return список [Project] владельца
     */
    override suspend fun listByOwnerId(ownerId: String): List<Project> =
        db
            .sql("SELECT * FROM projects WHERE owner_id = :ownerId ORDER BY created_at")
            .bind("ownerId", ownerId)
            .map { row, _ -> row.toProject() }
            .all()
            .collectList()
            .awaitSingle()

    /**
     * Удаление проекта по идентификатору.
     * @param id идентификатор проекта
     */
    override suspend fun delete(id: String) {
        db
            .sql("DELETE FROM projects WHERE id = :id")
            .bind("id", id)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    /**
     * Преобразование строки результата запроса R2DBC в доменную сущность [Project].
     * Считывает колонки таблицы `projects` и создаёт [ProjectTable], затем маппит в домен.
     * @param row строка результата запроса
     * @return доменная сущность [Project]
     */
    private fun io.r2dbc.spi.Row.toProject(): Project {
        val table =
            ProjectTable(
                id = get("id", String::class.java)!!,
                ownerId = get("owner_id", String::class.java)!!,
                name = get("name", String::class.java)!!,
                description = get("description", String::class.java),
                createdAt = get("created_at", java.time.LocalDateTime::class.java)!!,
                updatedAt = get("updated_at", java.time.LocalDateTime::class.java)!!,
            )
        return table.toDomain()
    }
}
