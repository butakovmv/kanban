package com.kanban.postgres.access

import com.kanban.access.Group
import com.kanban.access.GroupMember
import com.kanban.access.GroupMemberRepository
import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
internal class GroupMemberRepositoryImpl(
    private val db: DatabaseClient,
) : GroupMemberRepository {
    override suspend fun addMember(
        groupId: String,
        userId: String,
    ) {
        db
            .sql(
                """
                INSERT INTO group_members (group_id, user_id, added_at)
                VALUES (:groupId, :userId, :addedAt)
                """,
            ).bind("groupId", UUID.fromString(groupId))
            .bind("userId", UUID.fromString(userId))
            .bind("addedAt", LocalDateTime.now())
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    override suspend fun removeMember(
        groupId: String,
        userId: String,
    ) {
        db
            .sql("DELETE FROM group_members WHERE group_id = :groupId AND user_id = :userId")
            .bind("groupId", UUID.fromString(groupId))
            .bind("userId", UUID.fromString(userId))
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    override suspend fun listMembers(groupId: String): List<GroupMember> =
        db
            .sql("SELECT * FROM group_members WHERE group_id = :groupId")
            .bind("groupId", UUID.fromString(groupId))
            .map { row, _ -> row.toGroupMember() }
            .all()
            .collectList()
            .awaitSingle()

    override suspend fun listGroupsForUser(userId: String): List<Group> =
        db
            .sql(
                """
                SELECT g.* FROM groups g
                JOIN group_members gm ON g.id = gm.group_id
                WHERE gm.user_id = :userId
                ORDER BY g.name
                """,
            ).bind("userId", UUID.fromString(userId))
            .map { row, _ -> row.toGroup() }
            .all()
            .collectList()
            .awaitSingle()

    override suspend fun isMember(
        groupId: String,
        userId: String,
    ): Boolean {
        val count =
            db
                .sql(
                    """
                    SELECT COUNT(*) AS cnt FROM group_members
                    WHERE group_id = :groupId AND user_id = :userId
                    """,
                ).bind("groupId", UUID.fromString(groupId))
                .bind("userId", UUID.fromString(userId))
                .map { row, _ -> (row.get("cnt", java.lang.Long::class.java) ?: 0L) as Long }
                .one()
                .awaitSingle()
        return count > 0L
    }

    override suspend fun deleteAllByGroup(groupId: String) {
        db
            .sql("DELETE FROM group_members WHERE group_id = :groupId")
            .bind("groupId", UUID.fromString(groupId))
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
