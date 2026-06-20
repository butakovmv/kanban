package com.kanban.access

/**
 * Репозиторий для управления членством пользователей в группах.
 * Предоставляет методы добавления, удаления, проверки и выборки связей «пользователь-группа».
 */
interface GroupMemberRepository {
    /**
     * Добавляет пользователя в группу.
     *
     * @param groupId идентификатор группы
     * @param userId идентификатор пользователя
     */
    suspend fun addMember(
        groupId: String,
        userId: String,
    )

    /**
     * Удаляет пользователя из группы.
     *
     * @param groupId идентификатор группы
     * @param userId идентификатор пользователя
     */
    suspend fun removeMember(
        groupId: String,
        userId: String,
    )

    /**
     * Возвращает список членов указанной группы.
     *
     * @param groupId идентификатор группы
     * @return список членов группы
     */
    suspend fun listMembers(groupId: String): List<GroupMember>

    /**
     * Возвращает список групп, в которые входит указанный пользователь.
     *
     * @param userId идентификатор пользователя
     * @return список групп пользователя
     */
    suspend fun listGroupsForUser(userId: String): List<Group>

    /**
     * Проверяет, является ли пользователь членом группы.
     *
     * @param groupId идентификатор группы
     * @param userId идентификатор пользователя
     * @return true, если пользователь состоит в группе
     */
    suspend fun isMember(
        groupId: String,
        userId: String,
    ): Boolean

    /**
     * Удаляет все членства, связанные с указанной группой.
     * Используется при каскадном удалении группы.
     *
     * @param groupId идентификатор группы
     */
    suspend fun deleteAllByGroup(groupId: String)
}
