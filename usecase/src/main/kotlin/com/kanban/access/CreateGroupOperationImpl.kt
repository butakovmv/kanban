package com.kanban.access

import com.kanban.common.GroupId
import java.time.Instant
import java.util.UUID

/**
 * Реализация операции создания группы.
 * Валидирует название, генерирует идентификатор и сохраняет группу в репозитории.
 */
internal class CreateGroupOperationImpl(
    private val groupRepository: GroupRepository,
) : CreateGroupOperation {
    override suspend fun execute(arg: CreateGroupOperation.Arg): CreateGroupOperation.Result {
        if (arg.name.isBlank()) {
            return CreateGroupOperation.Result.Failure("Name must not be blank")
        }

        val group =
            Group(
                id = GroupId(UUID.randomUUID().toString()),
                name = arg.name.trim(),
                description = arg.description,
                createdAt = Instant.now(),
            )
        val saved = groupRepository.save(group)
        return CreateGroupOperation.Result.Success(saved)
    }
}
