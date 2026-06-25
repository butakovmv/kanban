package com.kanban.project

import com.kanban.sse.SinkService
import com.kanban.sse.SseEvent
import java.time.Instant

@Suppress("LongParameterList")
internal class BoardHandler(
    private val getBoardOperation: GetBoardOperation,
    private val createBoardOperation: CreateBoardOperation,
    private val updateBoardOperation: UpdateBoardOperation,
    private val deleteBoardOperation: DeleteBoardOperation,
    private val archiveBoardOperation: ArchiveBoardOperation,
    private val reorderColumnsOperation: ReorderColumnsOperation,
    private val listBoardsOperation: ListBoardsOperation,
    private val sinkService: SinkService? = null,
) {
    data class BoardData(
        val id: String,
        val projectId: String,
        val name: String,
        val position: Int,
        val createdAt: Instant,
    )

    data class ColumnData(
        val id: String,
        val projectId: String,
        val name: String,
        val position: Int,
        val wipLimit: Int?,
        val createdAt: Instant,
    )

    data class BoardViewData(
        val board: BoardData,
        val columns: List<ColumnData>,
    )

    suspend fun get(boardId: String): GetBoardResult {
        val result =
            getBoardOperation.execute(
                GetBoardOperation.Arg(boardId = boardId),
            )
        return when (result) {
            is GetBoardOperation.Result.Success ->
                GetBoardResult.Success(
                    view = result.view.toData(),
                )
            GetBoardOperation.Result.NotFound -> GetBoardResult.NotFound
        }
    }

    suspend fun getByProjectId(projectId: String): GetBoardResult {
        val boards = listBoardsOperation.execute(ListBoardsOperation.Arg(projectId = projectId))
        return when (boards) {
            is ListBoardsOperation.Result.Success -> {
                val board = boards.boards.firstOrNull() ?: return GetBoardResult.NotFound
                get(board.id.value)
            }
        }
    }

    suspend fun create(
        projectId: String,
        name: String,
    ): CreateBoardResult {
        val result =
            createBoardOperation.execute(
                CreateBoardOperation.Arg(
                    projectId = projectId,
                    name = name,
                ),
            )
        return when (result) {
            is CreateBoardOperation.Result.Success ->
                CreateBoardResult.Success(
                    view = result.view.toData(),
                )
            is CreateBoardOperation.Result.Failure ->
                CreateBoardResult.Failure(reason = result.reason)
        }
    }

    suspend fun update(
        boardId: String,
        name: String?,
    ): UpdateBoardResult {
        val result =
            updateBoardOperation.execute(
                UpdateBoardOperation.Arg(
                    boardId = boardId,
                    name = name,
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
                    board = result.board.toData(),
                )
            }
            UpdateBoardOperation.Result.NotFound -> UpdateBoardResult.NotFound
        }
    }

    private suspend fun resolveBoardId(projectId: String): String? {
        val boards = listBoardsOperation.execute(ListBoardsOperation.Arg(projectId = projectId))
        return when (boards) {
            is ListBoardsOperation.Result.Success ->
                boards.boards
                    .firstOrNull()
                    ?.id
                    ?.value
        }
    }

    suspend fun updateByProjectId(
        projectId: String,
        name: String?,
    ): UpdateBoardResult {
        val boardId = resolveBoardId(projectId) ?: return UpdateBoardResult.NotFound
        return update(boardId, name)
    }

    suspend fun delete(boardId: String): DeleteBoardResult {
        val result =
            deleteBoardOperation.execute(
                DeleteBoardOperation.Arg(boardId = boardId),
            )
        return when (result) {
            DeleteBoardOperation.Result.Success -> DeleteBoardResult.Success
            DeleteBoardOperation.Result.NotFound -> DeleteBoardResult.NotFound
        }
    }

    suspend fun deleteByProjectId(projectId: String): DeleteBoardResult {
        val boardId = resolveBoardId(projectId) ?: return DeleteBoardResult.NotFound
        return delete(boardId)
    }

    suspend fun archive(boardId: String): ArchiveBoardResult {
        val result =
            archiveBoardOperation.execute(
                ArchiveBoardOperation.Arg(boardId = boardId),
            )
        return when (result) {
            ArchiveBoardOperation.Result.Success -> {
                sinkService?.emit(
                    SseEvent(
                        type = "board_archived",
                        data = """{"board_id":"$boardId"}""",
                        boardId = boardId,
                        projectId = null,
                        timestamp = Instant.now(),
                    ),
                )
                ArchiveBoardResult.Success
            }
            ArchiveBoardOperation.Result.NotFound -> ArchiveBoardResult.NotFound
        }
    }

    suspend fun archiveByProjectId(projectId: String): ArchiveBoardResult {
        val boardId = resolveBoardId(projectId) ?: return ArchiveBoardResult.NotFound
        return archive(boardId)
    }

    suspend fun reorderColumns(
        boardId: String,
        columnIds: List<String>,
    ): ReorderColumnsResult {
        val result =
            reorderColumnsOperation.execute(
                ReorderColumnsOperation.Arg(
                    boardId = boardId,
                    columnIds = columnIds,
                ),
            )
        return when (result) {
            is ReorderColumnsOperation.Result.Success -> {
                sinkService?.emit(
                    SseEvent(
                        type = "columns_reordered",
                        data = """{"board_id":"$boardId"}""",
                        boardId = boardId,
                        projectId = null,
                        timestamp = Instant.now(),
                    ),
                )
                ReorderColumnsResult.Success(
                    columns = result.columns.map { it.toData() },
                )
            }
            ReorderColumnsOperation.Result.BoardNotFound -> ReorderColumnsResult.BoardNotFound
            ReorderColumnsOperation.Result.InvalidColumns -> ReorderColumnsResult.InvalidColumns
        }
    }

    suspend fun reorderColumnsByProjectId(
        projectId: String,
        columnIds: List<String>,
    ): ReorderColumnsResult {
        val boardId = resolveBoardId(projectId) ?: return ReorderColumnsResult.BoardNotFound
        return reorderColumns(boardId, columnIds)
    }

    private fun Board.toData(): BoardData =
        BoardData(
            id = id.value,
            projectId = projectId.value,
            name = name,
            position = position,
            createdAt = createdAt,
        )

    private fun Column.toData(): ColumnData =
        ColumnData(
            id = id.value,
            projectId = projectId.value,
            name = name,
            position = position,
            wipLimit = wipLimit,
            createdAt = createdAt,
        )

    private fun BoardView.toData(): BoardViewData =
        BoardViewData(
            board = board.toData(),
            columns = columns.map { it.toData() },
        )

    suspend fun listByProjectId(projectId: String): ListBoardsResult {
        val result =
            listBoardsOperation.execute(
                ListBoardsOperation.Arg(projectId = projectId),
            )
        return when (result) {
            is ListBoardsOperation.Result.Success ->
                ListBoardsResult.Success(
                    boards = result.boards.map { it.toData() },
                )
        }
    }

    sealed interface ListBoardsResult {
        data class Success(
            val boards: List<BoardData>,
        ) : ListBoardsResult
    }

    sealed interface GetBoardResult {
        data class Success(
            val view: BoardViewData,
        ) : GetBoardResult

        data object NotFound : GetBoardResult
    }

    sealed interface CreateBoardResult {
        data class Success(
            val view: BoardViewData,
        ) : CreateBoardResult

        data class Failure(
            val reason: String,
        ) : CreateBoardResult
    }

    sealed interface UpdateBoardResult {
        data class Success(
            val board: BoardData,
        ) : UpdateBoardResult

        data object NotFound : UpdateBoardResult
    }

    sealed interface DeleteBoardResult {
        data object Success : DeleteBoardResult

        data object NotFound : DeleteBoardResult
    }

    sealed interface ArchiveBoardResult {
        data object Success : ArchiveBoardResult

        data object NotFound : ArchiveBoardResult
    }

    sealed interface ReorderColumnsResult {
        data class Success(
            val columns: List<ColumnData>,
        ) : ReorderColumnsResult

        data object BoardNotFound : ReorderColumnsResult

        data object InvalidColumns : ReorderColumnsResult
    }
}
