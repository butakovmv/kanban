package com.kanban.project

import com.fasterxml.jackson.annotation.JsonProperty
import com.kanban.sse.SinkService
import com.kanban.sse.SseEvent
import java.time.Instant

/**
 * Обработчик запросов досок и колонок.
 * Связывает HTTP-контроллеры с usecase-операциями: преобразует DTO в аргументы операций,
 * вызывает операции и преобразует результаты обратно в DTO.
 *
 * @property getBoardOperation операция получения доски
 * @property createBoardOperation операция создания доски
 * @property updateBoardOperation операция обновления доски
 * @property deleteBoardOperation операция удаления доски
 * @property archiveBoardOperation операция архивирования доски
 * @property reorderColumnsOperation операция реордеринга колонок
 */
@Suppress("LongParameterList")
internal class BoardHandler(
    private val getBoardOperation: GetBoardOperation,
    private val createBoardOperation: CreateBoardOperation,
    private val updateBoardOperation: UpdateBoardOperation,
    private val deleteBoardOperation: DeleteBoardOperation,
    private val archiveBoardOperation: ArchiveBoardOperation,
    private val reorderColumnsOperation: ReorderColumnsOperation,
    private val sinkService: SinkService? = null,
) {
    /**
     * Получает доску вместе с её колонками.
     *
     * @param request идентификатор доски
     * @return результат с представлением доски или признак отсутствия
     */
    suspend fun get(request: GetBoardRequest): GetBoardResult {
        val result =
            getBoardOperation.execute(
                GetBoardOperation.Arg(boardId = request.boardId),
            )
        return when (result) {
            is GetBoardOperation.Result.Success ->
                GetBoardResult.Success(
                    view = result.view.toResponse(),
                )
            GetBoardOperation.Result.NotFound -> GetBoardResult.NotFound
        }
    }

    /**
     * Создаёт новую доску в проекте.
     *
     * @param request данные для создания доски
     * @return результат с созданной доской и колонками или ошибка
     */
    suspend fun create(request: CreateBoardRequest): CreateBoardResult {
        val result =
            createBoardOperation.execute(
                CreateBoardOperation.Arg(
                    projectId = request.projectId,
                    name = request.name,
                ),
            )
        return when (result) {
            is CreateBoardOperation.Result.Success ->
                CreateBoardResult.Success(
                    view = result.view.toResponse(),
                )
            is CreateBoardOperation.Result.Failure ->
                CreateBoardResult.Failure(reason = result.reason)
        }
    }

    /**
     * Обновляет поля доски.
     *
     * @param request данные для обновления
     * @return результат с обновлённой доской или признак отсутствия
     */
    suspend fun update(request: UpdateBoardRequest): UpdateBoardResult {
        val result =
            updateBoardOperation.execute(
                UpdateBoardOperation.Arg(
                    boardId = request.boardId,
                    name = request.name,
                ),
            )
        return when (result) {
            is UpdateBoardOperation.Result.Success -> {
                sinkService?.emit(
                    SseEvent(
                        type = "board_updated",
                        data = """{"board_id":"${result.board.id.value}"}""",
                        boardId = result.board.id.value,
                        projectId = result.board.projectId.value,
                        timestamp = Instant.now(),
                    ),
                )
                UpdateBoardResult.Success(
                    board = result.board.toResponse(),
                )
            }
            UpdateBoardOperation.Result.NotFound -> UpdateBoardResult.NotFound
        }
    }

    /**
     * Удаляет доску по идентификатору.
     *
     * @param request идентификатор доски
     * @return результат удаления
     */
    suspend fun delete(request: DeleteBoardRequest): DeleteBoardResult {
        val result =
            deleteBoardOperation.execute(
                DeleteBoardOperation.Arg(boardId = request.boardId),
            )
        return when (result) {
            DeleteBoardOperation.Result.Success -> DeleteBoardResult.Success
            DeleteBoardOperation.Result.NotFound -> DeleteBoardResult.NotFound
        }
    }

    /**
     * Архивирует доску.
     *
     * @param request идентификатор доски
     * @return результат архивирования
     */
    suspend fun archive(request: ArchiveBoardRequest): ArchiveBoardResult {
        val result =
            archiveBoardOperation.execute(
                ArchiveBoardOperation.Arg(boardId = request.boardId),
            )
        return when (result) {
            ArchiveBoardOperation.Result.Success -> {
                sinkService?.emit(
                    SseEvent(
                        type = "board_archived",
                        data = """{"board_id":"${request.boardId}"}""",
                        boardId = request.boardId,
                        projectId = null,
                        timestamp = Instant.now(),
                    ),
                )
                ArchiveBoardResult.Success
            }
            ArchiveBoardOperation.Result.NotFound -> ArchiveBoardResult.NotFound
        }
    }

    /**
     * Изменяет порядок колонок на доске.
     *
     * @param request идентификатор доски и список идентификаторов колонок в новом порядке
     * @return результат с обновлённым списком колонок или ошибка
     */
    suspend fun reorderColumns(request: ReorderColumnsRequest): ReorderColumnsResult {
        val result =
            reorderColumnsOperation.execute(
                ReorderColumnsOperation.Arg(
                    boardId = request.boardId,
                    columnIds = request.columnIds,
                ),
            )
        return when (result) {
            is ReorderColumnsOperation.Result.Success -> {
                sinkService?.emit(
                    SseEvent(
                        type = "columns_reordered",
                        data = """{"board_id":"${request.boardId}"}""",
                        boardId = request.boardId,
                        projectId = null,
                        timestamp = Instant.now(),
                    ),
                )
                ReorderColumnsResult.Success(
                    columns = result.columns.map { it.toResponse() },
                )
            }
            ReorderColumnsOperation.Result.BoardNotFound -> ReorderColumnsResult.BoardNotFound
            ReorderColumnsOperation.Result.InvalidColumns -> ReorderColumnsResult.InvalidColumns
        }
    }

    /**
     * Преобразование сущности доски в DTO ответа.
     */
    private fun Board.toResponse(): BoardResponse =
        BoardResponse(
            id = id.value,
            projectId = projectId.value,
            name = name,
            position = position,
            createdAt = createdAt,
        )

    /**
     * Преобразование сущности колонки в DTO ответа.
     */
    private fun Column.toResponse(): ColumnResponse =
        ColumnResponse(
            id = id.value,
            boardId = boardId.value,
            name = name,
            position = position,
            wipLimit = wipLimit,
            createdAt = createdAt,
        )

    /**
     * Преобразование представления доски в DTO ответа.
     */
    private fun BoardView.toResponse(): BoardViewResponse =
        BoardViewResponse(
            board = board.toResponse(),
            columns = columns.map { it.toResponse() },
        )

    /**
     * DTO запроса получения доски.
     *
     * @property boardId идентификатор доски
     */
    data class GetBoardRequest(
        @JsonProperty("board_id")
        val boardId: String,
    )

    /**
     * DTO запроса создания доски.
     *
     * @property projectId идентификатор проекта
     * @property name название доски
     */
    data class CreateBoardRequest(
        @JsonProperty("project_id")
        val projectId: String,
        val name: String,
    )

    /**
     * DTO тела запроса обновления доски.
     *
     * @property name новое название (null — не изменять)
     */
    data class UpdateBoardBody(
        val name: String?,
    )

    /**
     * DTO запроса обновления доски (идентификатор берётся из пути).
     *
     * @property boardId идентификатор доски
     * @property name новое название (null — не изменять)
     */
    data class UpdateBoardRequest(
        @JsonProperty("board_id")
        val boardId: String,
        val name: String?,
    )

    /**
     * DTO запроса удаления доски.
     *
     * @property boardId идентификатор доски
     */
    data class DeleteBoardRequest(
        @JsonProperty("board_id")
        val boardId: String,
    )

    /**
     * DTO запроса архивирования доски.
     *
     * @property boardId идентификатор доски
     */
    data class ArchiveBoardRequest(
        @JsonProperty("board_id")
        val boardId: String,
    )

    /**
     * DTO тела запроса реордеринга колонок.
     *
     * @property columnIds идентификаторы колонок в новом порядке
     */
    data class ReorderColumnsBody(
        @JsonProperty("column_ids")
        val columnIds: List<String>,
    )

    /**
     * DTO запроса реордеринга колонок (идентификатор доски берётся из пути).
     *
     * @property boardId идентификатор доски
     * @property columnIds идентификаторы колонок в новом порядке
     */
    data class ReorderColumnsRequest(
        @JsonProperty("board_id")
        val boardId: String,
        @JsonProperty("column_ids")
        val columnIds: List<String>,
    )

    /**
     * DTO ответа с доской.
     *
     * @property id идентификатор доски
     * @property projectId идентификатор проекта
     * @property name название доски
     * @property position позиция доски в проекте
     * @property createdAt дата создания
     */
    data class BoardResponse(
        val id: String,
        @JsonProperty("project_id")
        val projectId: String,
        val name: String,
        val position: Int,
        @JsonProperty("created_at")
        val createdAt: Instant,
    )

    /**
     * DTO ответа с колонкой.
     *
     * @property id идентификатор колонки
     * @property boardId идентификатор доски
     * @property name название колонки
     * @property position позиция колонки
     * @property wipLimit WIP-лимит
     * @property createdAt дата создания
     */
    data class ColumnResponse(
        val id: String,
        @JsonProperty("board_id")
        val boardId: String,
        val name: String,
        val position: Int,
        @JsonProperty("wip_limit")
        val wipLimit: Int?,
        @JsonProperty("created_at")
        val createdAt: Instant,
    )

    /**
     * DTO ответа с представлением доски.
     *
     * @property board доска
     * @property columns список колонок в порядке позиций
     */
    data class BoardViewResponse(
        val board: BoardResponse,
        val columns: List<ColumnResponse>,
    )

    /**
     * Результат операции получения доски.
     */
    sealed interface GetBoardResult {
        /** Доска найдена. */
        data class Success(
            val view: BoardViewResponse,
        ) : GetBoardResult

        /** Доска не найдена. */
        data object NotFound : GetBoardResult
    }

    /**
     * Результат операции создания доски.
     */
    sealed interface CreateBoardResult {
        /** Доска успешно создана. */
        data class Success(
            val view: BoardViewResponse,
        ) : CreateBoardResult

        /** Ошибка создания доски. */
        data class Failure(
            val reason: String,
        ) : CreateBoardResult
    }

    /**
     * Результат операции обновления доски.
     */
    sealed interface UpdateBoardResult {
        /** Доска успешно обновлена. */
        data class Success(
            val board: BoardResponse,
        ) : UpdateBoardResult

        /** Доска не найдена. */
        data object NotFound : UpdateBoardResult
    }

    /**
     * Результат операции удаления доски.
     */
    sealed interface DeleteBoardResult {
        /** Доска успешно удалена. */
        data object Success : DeleteBoardResult

        /** Доска не найдена. */
        data object NotFound : DeleteBoardResult
    }

    /**
     * Результат операции архивирования доски.
     */
    sealed interface ArchiveBoardResult {
        /** Доска успешно архивирована. */
        data object Success : ArchiveBoardResult

        /** Доска не найдена. */
        data object NotFound : ArchiveBoardResult
    }

    /**
     * Результат операции реордеринга колонок.
     */
    sealed interface ReorderColumnsResult {
        /** Колонки успешно реордерены. */
        data class Success(
            val columns: List<ColumnResponse>,
        ) : ReorderColumnsResult

        /** Доска не найдена. */
        data object BoardNotFound : ReorderColumnsResult

        /** Набор переданных колонок не совпадает с текущими колонками доски. */
        data object InvalidColumns : ReorderColumnsResult
    }
}
