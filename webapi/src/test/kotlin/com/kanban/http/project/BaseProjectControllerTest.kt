package com.kanban.http.project

import com.kanban.project.ArchiveBoardOperation
import com.kanban.project.BoardHandler
import com.kanban.project.CreateBoardOperation
import com.kanban.project.CreateProjectOperation
import com.kanban.project.DeleteBoardOperation
import com.kanban.project.DeleteProjectOperation
import com.kanban.project.GetBoardOperation
import com.kanban.project.GetProjectOperation
import com.kanban.project.ListBoardsOperation
import com.kanban.project.ListProjectsOperation
import com.kanban.project.ProjectHandler
import com.kanban.project.ReorderColumnsOperation
import com.kanban.project.UpdateBoardOperation
import com.kanban.project.UpdateProjectOperation
import io.mockk.mockk
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Базовый класс для тестов контроллеров проектов и досок.
 * Создаёт WebTestClient с прямой привязкой к контроллерам.
 * Моки usecase-операций создаются автоматически и доступны как поля теста.
 *
 * Примечание: для snake_case сериализации используются @JsonProperty аннотации на DTO,
 * что позволяет работать с дефолтным ObjectMapper в тестовом окружении.
 */
internal abstract class BaseProjectControllerTest {
    protected lateinit var createProjectOperation: CreateProjectOperation
    protected lateinit var getProjectOperation: GetProjectOperation
    protected lateinit var listProjectsOperation: ListProjectsOperation
    protected lateinit var updateProjectOperation: UpdateProjectOperation
    protected lateinit var deleteProjectOperation: DeleteProjectOperation
    protected lateinit var getBoardOperation: GetBoardOperation
    protected lateinit var createBoardOperation: CreateBoardOperation
    protected lateinit var updateBoardOperation: UpdateBoardOperation
    protected lateinit var deleteBoardOperation: DeleteBoardOperation
    protected lateinit var archiveBoardOperation: ArchiveBoardOperation
    protected lateinit var reorderColumnsOperation: ReorderColumnsOperation
    protected lateinit var listBoardsOperation: ListBoardsOperation

    /**
     * Создаёт WebTestClient привязанный к указанному контроллеру.
     * Моки usecase-операций создаются автоматически и доступны как поля теста.
     *
     * @param controllerClass класс контроллера для тестирования
     * @return настроенный WebTestClient
     */
    protected fun bindTo(controllerClass: Class<*>): WebTestClient {
        createProjectOperation = mockk()
        getProjectOperation = mockk()
        listProjectsOperation = mockk()
        updateProjectOperation = mockk()
        deleteProjectOperation = mockk()
        getBoardOperation = mockk()
        createBoardOperation = mockk()
        updateBoardOperation = mockk()
        deleteBoardOperation = mockk()
        archiveBoardOperation = mockk()
        reorderColumnsOperation = mockk()
        listBoardsOperation = mockk()

        val projectHandler =
            ProjectHandler(
                createProjectOperation = createProjectOperation,
                getProjectOperation = getProjectOperation,
                listProjectsOperation = listProjectsOperation,
                updateProjectOperation = updateProjectOperation,
                deleteProjectOperation = deleteProjectOperation,
            )

        val boardHandler =
            BoardHandler(
                getBoardOperation = getBoardOperation,
                createBoardOperation = createBoardOperation,
                updateBoardOperation = updateBoardOperation,
                deleteBoardOperation = deleteBoardOperation,
                archiveBoardOperation = archiveBoardOperation,
                reorderColumnsOperation = reorderColumnsOperation,
                listBoardsOperation = listBoardsOperation,
            )

        val controller =
            when (controllerClass) {
                CreateProjectController::class.java -> CreateProjectController(projectHandler)
                GetProjectController::class.java -> GetProjectController(projectHandler)
                ListProjectsController::class.java -> ListProjectsController(projectHandler)
                UpdateProjectController::class.java -> UpdateProjectController(projectHandler)
                DeleteProjectController::class.java -> DeleteProjectController(projectHandler)
                GetBoardController::class.java -> GetBoardController(boardHandler)
                CreateBoardController::class.java -> CreateBoardController(boardHandler)
                UpdateBoardController::class.java -> UpdateBoardController(boardHandler)
                DeleteBoardController::class.java -> DeleteBoardController(boardHandler)
                ArchiveBoardController::class.java -> ArchiveBoardController(boardHandler)
                ReorderColumnsController::class.java -> ReorderColumnsController(boardHandler)
                ListProjectBoardsController::class.java -> ListProjectBoardsController(boardHandler)
                else -> throw IllegalArgumentException("Unsupported controller: $controllerClass")
            }

        return WebTestClient.bindToController(controller).build()
    }
}
