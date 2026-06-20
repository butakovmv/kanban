package com.kanban.access

/**
 * Реализация операции обновления группы.
 * Находит группу по ID, обновляет указанные поля (name, description) и сохраняет.
 */
internal class UpdateGroupOperationImpl(
    private val groupRepository: GroupRepository,
) : UpdateGroupOperation {
    override suspend fun execute(arg: UpdateGroupOperation.Arg): UpdateGroupOperation.Result {
        val existing = groupRepository.findById(arg.groupId) ?: return UpdateGroupOperation.Result.NotFound
        if (arg.name != null && arg.name.isBlank()) {
            return UpdateGroupOperation.Result.Failure("Name must not be blank")
        }
        val updated =
            existing.copy(
                name = arg.name?.trim() ?: existing.name,
                description = arg.description ?: existing.description,
            )
        val saved = groupRepository.save(updated)
        return UpdateGroupOperation.Result.Success(saved)
    }
}
