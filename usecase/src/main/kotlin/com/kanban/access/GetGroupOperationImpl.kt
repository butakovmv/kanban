package com.kanban.access

/**
 * Реализация операции получения группы.
 * Делегирует поиск в репозиторий групп.
 */
internal class GetGroupOperationImpl(
    private val groupRepository: GroupRepository,
) : GetGroupOperation {
    override suspend fun execute(arg: GetGroupOperation.Arg): GetGroupOperation.Result {
        val group = groupRepository.findById(arg.groupId) ?: return GetGroupOperation.Result.NotFound
        return GetGroupOperation.Result.Success(group)
    }
}
