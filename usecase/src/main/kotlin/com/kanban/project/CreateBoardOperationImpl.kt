package com.kanban.project

import com.kanban.common.BoardId
import com.kanban.common.ColumnId
import com.kanban.common.ProjectId
import com.kanban.identity.CheckTariffLimitsOperation
import java.time.Instant
import java.util.UUID

/**
 * Реализация операции создания доски.
 * Проверяет лимиты тарифа, проверяет существование проекта, создаёт доску
 * с набором колонок по умолчанию ("To Do", "In Progress", "Done") и возвращает [BoardView].
 */
internal class CreateBoardOperationImpl(
    private val projectRepository: ProjectRepository,
    private val boardRepository: BoardRepository,
    private val columnRepository: ColumnRepository,
    private val checkTariffLimitsOperation: CheckTariffLimitsOperation,
) : CreateBoardOperation {
    override suspend fun execute(arg: CreateBoardOperation.Arg): CreateBoardOperation.Result {
        val project =
            projectRepository.findById(arg.projectId)
                ?: return CreateBoardOperation.Result.Failure("Project not found")

        val limitCheck =
            checkTariffLimitsOperation.execute(
                CheckTariffLimitsOperation.Arg(
                    userId = project.ownerId.value,
                    resourceType = CheckTariffLimitsOperation.ResourceType.BOARD,
                    requestedCount = 1,
                ),
            )
        if (limitCheck is CheckTariffLimitsOperation.Result.Denied) {
            return CreateBoardOperation.Result.Failure(limitCheck.reason)
        }

        val now = Instant.now()
        val boardId = BoardId(UUID.randomUUID().toString())

        val existingBoards = boardRepository.listByProjectId(arg.projectId)
        val board =
            Board(
                id = boardId,
                projectId = ProjectId(arg.projectId),
                name = arg.name,
                position = existingBoards.size,
                createdAt = now,
            )

        val savedBoard = boardRepository.save(board)

        val defaultColumnNames = listOf("To Do", "In Progress", "Done")
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
        val savedColumns = columns.map { columnRepository.save(it) }

        return CreateBoardOperation.Result.Success(
            BoardView(board = savedBoard, columns = savedColumns),
        )
    }
}
