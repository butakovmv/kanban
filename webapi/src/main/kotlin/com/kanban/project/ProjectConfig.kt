package com.kanban.project

import com.kanban.sse.SinkService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Конфигурация доменных обработчиков проектов и досок.
 * Регистрирует [ProjectHandler] и [BoardHandler] как Spring-бины,
 * связывая их с usecase-операциями.
 */
@Configuration
internal class ProjectConfig {
    @Bean
    @Suppress("LongParameterList")
    fun projectHandler(
        createProjectOperation: CreateProjectOperation,
        getProjectOperation: GetProjectOperation,
        listProjectsOperation: ListProjectsOperation,
        updateProjectOperation: UpdateProjectOperation,
        deleteProjectOperation: DeleteProjectOperation,
        listProjectMembersOperation: ListProjectMembersOperation,
        addProjectMemberOperation: AddProjectMemberOperation,
        removeProjectMemberOperation: RemoveProjectMemberOperation,
        listMemberProjectsOperation: ListMemberProjectsOperation,
    ): ProjectHandler =
        ProjectHandler(
            createProjectOperation = createProjectOperation,
            getProjectOperation = getProjectOperation,
            listProjectsOperation = listProjectsOperation,
            updateProjectOperation = updateProjectOperation,
            deleteProjectOperation = deleteProjectOperation,
            listProjectMembersOperation = listProjectMembersOperation,
            addProjectMemberOperation = addProjectMemberOperation,
            removeProjectMemberOperation = removeProjectMemberOperation,
            listMemberProjectsOperation = listMemberProjectsOperation,
        )

    /**
     * Создаёт обработчик запросов досок.
     *
     * @param getBoardOperation операция получения доски
     * @param createBoardOperation операция создания доски
     * @param updateBoardOperation операция обновления доски
     * @param deleteBoardOperation операция удаления доски
     * @param archiveBoardOperation операция архивирования доски
     * @param reorderColumnsOperation операция реордеринга колонок
     * @return экземпляр [BoardHandler]
     */
    @Bean
    @Suppress("LongParameterList")
    fun boardHandler(
        getBoardOperation: GetBoardOperation,
        createBoardOperation: CreateBoardOperation,
        updateBoardOperation: UpdateBoardOperation,
        deleteBoardOperation: DeleteBoardOperation,
        archiveBoardOperation: ArchiveBoardOperation,
        reorderColumnsOperation: ReorderColumnsOperation,
        listBoardsOperation: ListBoardsOperation,
        sinkService: SinkService,
    ): BoardHandler =
        BoardHandler(
            getBoardOperation = getBoardOperation,
            createBoardOperation = createBoardOperation,
            updateBoardOperation = updateBoardOperation,
            deleteBoardOperation = deleteBoardOperation,
            archiveBoardOperation = archiveBoardOperation,
            reorderColumnsOperation = reorderColumnsOperation,
            listBoardsOperation = listBoardsOperation,
            sinkService = sinkService,
        )
}
