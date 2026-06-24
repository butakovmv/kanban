package com.kanban.task

import com.kanban.audit.LogAuditEventOperation
import com.kanban.sse.SinkService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Конфигурация доменных обработчиков задач, комментариев и файлов.
 * Регистрирует [TaskHandler], [CommentHandler] и [FileHandler] как Spring-бины,
 * связывая их с usecase-операциями.
 */
@Configuration
internal class TaskConfig {
    /**
     * Создаёт обработчик запросов задач.
     *
     * @param createTaskOperation операция создания задачи
     * @param getTaskOperation операция получения задачи
     * @param listTasksOperation операция получения списка задач
     * @param listBoardBacklogOperation операция получения бэклога доски
     * @param listArchivedTasksOperation операция получения архивированных задач доски
     * @param updateTaskOperation операция обновления задачи
     * @param moveTaskOperation операция перемещения задачи
     * @param archiveTaskOperation операция архивирования задачи
     * @param deleteTaskOperation операция удаления задачи
     * @param logAuditEventOperation операция записи аудит-логов
     * @return экземпляр [TaskHandler]
     */
    @Bean
    @Suppress("LongParameterList")
    fun taskHandler(
        createTaskOperation: CreateTaskOperation,
        getTaskOperation: GetTaskOperation,
        listTasksOperation: ListTasksOperation,
        listBoardBacklogOperation: ListBoardBacklogOperation,
        listArchivedTasksOperation: ListArchivedTasksOperation,
        updateTaskOperation: UpdateTaskOperation,
        moveTaskOperation: MoveTaskOperation,
        archiveTaskOperation: ArchiveTaskOperation,
        deleteTaskOperation: DeleteTaskOperation,
        logAuditEventOperation: LogAuditEventOperation,
        sinkService: SinkService,
    ): TaskHandler =
        TaskHandler(
            createTaskOperation = createTaskOperation,
            getTaskOperation = getTaskOperation,
            listTasksOperation = listTasksOperation,
            listBoardBacklogOperation = listBoardBacklogOperation,
            listArchivedTasksOperation = listArchivedTasksOperation,
            updateTaskOperation = updateTaskOperation,
            moveTaskOperation = moveTaskOperation,
            archiveTaskOperation = archiveTaskOperation,
            deleteTaskOperation = deleteTaskOperation,
            logAuditEventOperation = logAuditEventOperation,
            sinkService = sinkService,
        )

    /**
     * Создаёт обработчик запросов комментариев.
     *
     * @param createCommentOperation операция создания комментария
     * @param updateCommentOperation операция обновления комментария
     * @param deleteCommentOperation операция удаления комментария
     * @param listCommentsOperation операция получения списка комментариев
     * @return экземпляр [CommentHandler]
     */
    @Bean
    fun commentHandler(
        createCommentOperation: CreateCommentOperation,
        updateCommentOperation: UpdateCommentOperation,
        deleteCommentOperation: DeleteCommentOperation,
        listCommentsOperation: ListCommentsOperation,
        getTaskOperation: GetTaskOperation,
        sinkService: SinkService,
    ): CommentHandler =
        CommentHandler(
            createCommentOperation = createCommentOperation,
            updateCommentOperation = updateCommentOperation,
            deleteCommentOperation = deleteCommentOperation,
            listCommentsOperation = listCommentsOperation,
            getTaskOperation = getTaskOperation,
            sinkService = sinkService,
        )

    /**
     * Создаёт обработчик запросов прикреплённых файлов.
     *
     * @param attachFileOperation операция прикрепления файла
     * @param deleteFileOperation операция удаления файла
     * @param listFilesOperation операция получения списка файлов
     * @param getFileDownloadUrlOperation операция получения URL для скачивания
     * @return экземпляр [FileHandler]
     */
    @Bean
    fun fileHandler(
        attachFileOperation: AttachFileOperation,
        deleteFileOperation: DeleteFileOperation,
        listFilesOperation: ListFilesOperation,
        getFileDownloadUrlOperation: GetFileDownloadUrlOperation,
    ): FileHandler =
        FileHandler(
            attachFileOperation = attachFileOperation,
            deleteFileOperation = deleteFileOperation,
            listFilesOperation = listFilesOperation,
            getFileDownloadUrlOperation = getFileDownloadUrlOperation,
        )
}
