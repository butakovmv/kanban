package com.kanban.project

import com.kanban.common.BoardId
import com.kanban.common.ColumnId
import com.kanban.common.ProjectId
import com.kanban.common.UserId
import com.kanban.identity.CheckTariffLimitsOperation
import java.time.Instant
import java.util.UUID

/**
 * Реализация операции создания проекта.
 * Проверяет лимиты тарифа пользователя, создаёт сущность проекта,
 * создаёт доску по умолчанию с колонками и сохраняет всё в репозиторий.
 */
internal class CreateProjectOperationImpl(
    private val projectRepository: ProjectRepository,
    private val boardRepository: BoardRepository,
    private val columnRepository: ColumnRepository,
    private val checkTariffLimitsOperation: CheckTariffLimitsOperation,
) : CreateProjectOperation {
    override suspend fun execute(arg: CreateProjectOperation.Arg): CreateProjectOperation.Result {
        val limitCheck =
            checkTariffLimitsOperation.execute(
                CheckTariffLimitsOperation.Arg(
                    userId = arg.ownerId,
                    resourceType = CheckTariffLimitsOperation.ResourceType.PROJECT,
                    requestedCount = 1,
                ),
            )
        if (limitCheck is CheckTariffLimitsOperation.Result.Denied) {
            return CreateProjectOperation.Result.Failure(limitCheck.reason)
        }

        val now = Instant.now()
        val projectId = ProjectId(UUID.randomUUID().toString())
        val project =
            Project(
                id = projectId,
                ownerId = UserId(arg.ownerId),
                name = arg.name,
                description = arg.description,
                createdAt = now,
                updatedAt = now,
            )

        val savedProject = projectRepository.save(project)

        val boardId = BoardId(UUID.randomUUID().toString())
        val board =
            Board(
                id = boardId,
                projectId = projectId,
                name = "Main",
                position = 0,
                createdAt = now,
            )
        boardRepository.save(board)

        val defaultColumnNames = listOf("Backlog", "To Do", "In Progress", "Done")
        val columns =
            defaultColumnNames.mapIndexed { index, name ->
                Column(
                    id = ColumnId(UUID.randomUUID().toString()),
                    boardId = boardId,
                    name = name,
                    position = index,
                    wipLimit = null,
                    createdAt = now,
                )
            }
        columns.forEach { columnRepository.save(it) }

        return CreateProjectOperation.Result.Success(savedProject)
    }
}
