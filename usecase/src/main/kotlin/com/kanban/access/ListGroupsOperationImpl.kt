package com.kanban.access

/**
 * Реализация операции получения списка групп.
 * Делегирует запрос в репозиторий групп.
 */
internal class ListGroupsOperationImpl(
    private val groupRepository: GroupRepository,
) : ListGroupsOperation {
    override suspend fun execute(arg: ListGroupsOperation.Arg): ListGroupsOperation.Result {
        val groups = groupRepository.listAll()
        return ListGroupsOperation.Result.Success(groups)
    }
}
