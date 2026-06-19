package com.kanban.project

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

/**
 * Обработчик запросов проектов.
 * Связывает HTTP-контроллеры с usecase-операциями: преобразует DTO в аргументы операций,
 * вызывает операции и преобразует результаты обратно в DTO.
 *
 * @property createProjectOperation операция создания проекта
 * @property getProjectOperation операция получения проекта
 * @property listProjectsOperation операция получения списка проектов
 * @property updateProjectOperation операция обновления проекта
 * @property deleteProjectOperation операция удаления проекта
 */
internal class ProjectHandler(
    private val createProjectOperation: CreateProjectOperation,
    private val getProjectOperation: GetProjectOperation,
    private val listProjectsOperation: ListProjectsOperation,
    private val updateProjectOperation: UpdateProjectOperation,
    private val deleteProjectOperation: DeleteProjectOperation,
) {
    /**
     * Создаёт новый проект.
     *
     * @param request данные для создания проекта
     * @return результат с созданным проектом или ошибка
     */
    suspend fun create(request: CreateProjectRequest): CreateProjectResult {
        val result =
            createProjectOperation.execute(
                CreateProjectOperation.Arg(
                    ownerId = request.ownerId,
                    name = request.name,
                    description = request.description,
                ),
            )
        return when (result) {
            is CreateProjectOperation.Result.Success ->
                CreateProjectResult.Success(
                    project = result.project.toResponse(),
                )
            is CreateProjectOperation.Result.Failure ->
                CreateProjectResult.Failure(reason = result.reason)
        }
    }

    /**
     * Получает проект по идентификатору.
     *
     * @param request идентификатор проекта
     * @return результат с проектом или признак отсутствия
     */
    suspend fun get(request: GetProjectRequest): GetProjectResult {
        val result =
            getProjectOperation.execute(
                GetProjectOperation.Arg(projectId = request.projectId),
            )
        return when (result) {
            is GetProjectOperation.Result.Success ->
                GetProjectResult.Success(
                    project = result.project.toResponse(),
                )
            GetProjectOperation.Result.NotFound -> GetProjectResult.NotFound
        }
    }

    /**
     * Получает список проектов пользователя.
     *
     * @param request идентификатор владельца
     * @return результат со списком проектов
     */
    suspend fun list(request: ListProjectsRequest): ListProjectsResult {
        val result =
            listProjectsOperation.execute(
                ListProjectsOperation.Arg(ownerId = request.ownerId),
            )
        return when (result) {
            is ListProjectsOperation.Result.Success ->
                ListProjectsResult.Success(
                    projects = result.projects.map { it.toResponse() },
                )
        }
    }

    /**
     * Обновляет поля проекта.
     *
     * @param request данные для обновления
     * @return результат с обновлённым проектом, ошибка валидации или признак отсутствия
     */
    suspend fun update(request: UpdateProjectRequest): UpdateProjectResult {
        val result =
            updateProjectOperation.execute(
                UpdateProjectOperation.Arg(
                    projectId = request.projectId,
                    name = request.name,
                    description = request.description,
                ),
            )
        return when (result) {
            is UpdateProjectOperation.Result.Success ->
                UpdateProjectResult.Success(
                    project = result.project.toResponse(),
                )
            UpdateProjectOperation.Result.NotFound -> UpdateProjectResult.NotFound
        }
    }

    /**
     * Удаляет проект по идентификатору.
     *
     * @param request идентификатор проекта
     * @return результат удаления
     */
    suspend fun delete(request: DeleteProjectRequest): DeleteProjectResult {
        val result =
            deleteProjectOperation.execute(
                DeleteProjectOperation.Arg(projectId = request.projectId),
            )
        return when (result) {
            DeleteProjectOperation.Result.Success -> DeleteProjectResult.Success
            DeleteProjectOperation.Result.NotFound -> DeleteProjectResult.NotFound
        }
    }

    /**
     * Преобразование сущности проекта в DTO ответа.
     */
    private fun Project.toResponse(): ProjectResponse =
        ProjectResponse(
            id = id.value,
            ownerId = ownerId.value,
            name = name,
            description = description,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    /**
     * DTO запроса создания проекта.
     *
     * @property ownerId идентификатор владельца
     * @property name название проекта
     * @property description описание проекта (опционально)
     */
    data class CreateProjectRequest(
        @JsonProperty("owner_id")
        val ownerId: String,
        val name: String,
        val description: String?,
    )

    /**
     * DTO запроса получения проекта.
     *
     * @property projectId идентификатор проекта
     */
    data class GetProjectRequest(
        @JsonProperty("project_id")
        val projectId: String,
    )

    /**
     * DTO запроса списка проектов.
     *
     * @property ownerId идентификатор владельца
     */
    data class ListProjectsRequest(
        @JsonProperty("owner_id")
        val ownerId: String,
    )

    /**
     * DTO тела запроса обновления проекта.
     *
     * @property name новое название (null — не изменять)
     * @property description новое описание (null — не изменять)
     */
    data class UpdateProjectBody(
        val name: String?,
        val description: String?,
    )

    /**
     * DTO запроса обновления проекта (идентификатор берётся из пути).
     *
     * @property projectId идентификатор проекта
     * @property name новое название (null — не изменять)
     * @property description новое описание (null — не изменять)
     */
    data class UpdateProjectRequest(
        @JsonProperty("project_id")
        val projectId: String,
        val name: String?,
        val description: String?,
    )

    /**
     * DTO запроса удаления проекта.
     *
     * @property projectId идентификатор проекта
     */
    data class DeleteProjectRequest(
        @JsonProperty("project_id")
        val projectId: String,
    )

    /**
     * DTO ответа с проектом.
     *
     * @property id идентификатор проекта
     * @property ownerId идентификатор владельца
     * @property name название проекта
     * @property description описание проекта
     * @property createdAt дата создания
     * @property updatedAt дата последнего изменения
     */
    data class ProjectResponse(
        val id: String,
        @JsonProperty("owner_id")
        val ownerId: String,
        val name: String,
        val description: String?,
        @JsonProperty("created_at")
        val createdAt: Instant,
        @JsonProperty("updated_at")
        val updatedAt: Instant,
    )

    /**
     * Результат операции создания проекта.
     */
    sealed interface CreateProjectResult {
        /** Проект успешно создан. */
        data class Success(
            val project: ProjectResponse,
        ) : CreateProjectResult

        /** Ошибка создания проекта. */
        data class Failure(
            val reason: String,
        ) : CreateProjectResult
    }

    /**
     * Результат операции получения проекта.
     */
    sealed interface GetProjectResult {
        /** Проект найден. */
        data class Success(
            val project: ProjectResponse,
        ) : GetProjectResult

        /** Проект не найден. */
        data object NotFound : GetProjectResult
    }

    /**
     * Результат операции получения списка проектов.
     */
    sealed interface ListProjectsResult {
        /** Список проектов успешно получен. */
        data class Success(
            val projects: List<ProjectResponse>,
        ) : ListProjectsResult
    }

    /**
     * Результат операции обновления проекта.
     */
    sealed interface UpdateProjectResult {
        /** Проект успешно обновлён. */
        data class Success(
            val project: ProjectResponse,
        ) : UpdateProjectResult

        /** Проект не найден. */
        data object NotFound : UpdateProjectResult
    }

    /**
     * Результат операции удаления проекта.
     */
    sealed interface DeleteProjectResult {
        /** Проект успешно удалён. */
        data object Success : DeleteProjectResult

        /** Проект не найден. */
        data object NotFound : DeleteProjectResult
    }
}
