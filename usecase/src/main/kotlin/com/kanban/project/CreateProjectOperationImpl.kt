package com.kanban.project

import com.kanban.common.ProjectId
import com.kanban.common.UserId
import com.kanban.identity.CheckTariffLimitsOperation
import java.time.Instant
import java.util.UUID

/**
 * Реализация операции создания проекта.
 * Проверяет лимиты тарифа пользователя, создаёт сущность проекта и сохраняет в репозиторий.
 */
internal class CreateProjectOperationImpl(
    private val projectRepository: ProjectRepository,
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
        val project =
            Project(
                id = ProjectId(UUID.randomUUID().toString()),
                ownerId = UserId(arg.ownerId),
                name = arg.name,
                description = arg.description,
                createdAt = now,
                updatedAt = now,
            )

        val saved = projectRepository.save(project)
        return CreateProjectOperation.Result.Success(saved)
    }
}
