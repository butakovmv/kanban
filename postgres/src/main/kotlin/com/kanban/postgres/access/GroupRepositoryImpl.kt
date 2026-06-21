package com.kanban.postgres.access

import com.kanban.access.Group
import com.kanban.access.GroupRepository
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
internal class GroupRepositoryImpl(
    private val db: DatabaseClient,
) : GroupRepository {
    override suspend fun save(group: Group): Group {
        val z = ZoneId.systemDefault()
        val createdAt = group.createdAt.atZone(z).toLocalDateTime()
        if (findById(group.id.value) != null) {
            updateGroup(group)
        } else {
            insertGroup(group, createdAt)
        }
        return group
    }

    private suspend fun updateGroup(group: Group) {
        db
            .sql(
                """
                UPDATE groups SET
                    name = :name, description = :description
                WHERE id = :id
                """,
            ).bind("id", UUID.fromString(group.id.value))
            .bind("name", group.name)
            .let { spec ->
                val description = group.description
                if (description != null) {
                    spec.bind("description", description)
                } else {
                    spec.bindNull("description", String::class.java)
                }
            }.fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    private suspend fun insertGroup(
        group: Group,
        createdAt: LocalDateTime,
    ) {
        db
            .sql(
                """
                INSERT INTO groups (id, name, description, created_at)
                VALUES (:id, :name, :description, :createdAt)
                """,
            ).bind("id", UUID.fromString(group.id.value))
            .bind("name", group.name)
            .let { spec ->
                val description = group.description
                if (description != null) {
                    spec.bind("description", description)
                } else {
                    spec.bindNull("description", String::class.java)
                }
            }.bind("createdAt", createdAt)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    override suspend fun findById(id: String): Group? =
        db
            .sql("SELECT * FROM groups WHERE id = :id")
            .bind("id", UUID.fromString(id))
            .map { row, _ -> row.toGroup() }
            .one()
            .awaitFirstOrNull()

    override suspend fun listAll(): List<Group> =
        db
            .sql("SELECT * FROM groups ORDER BY name")
            .map { row, _ -> row.toGroup() }
            .all()
            .collectList()
            .awaitSingle()

    override suspend fun delete(id: String) {
        db
            .sql("DELETE FROM groups WHERE id = :id")
            .bind("id", UUID.fromString(id))
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
