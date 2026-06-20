package com.kanban.http.task

import com.kanban.task.ArchiveTaskOperation
import com.kanban.task.AttachFileOperation
import com.kanban.task.CommentHandler
import com.kanban.task.CreateCommentOperation
import com.kanban.task.CreateTaskOperation
import com.kanban.task.DeleteCommentOperation
import com.kanban.task.DeleteFileOperation
import com.kanban.task.DeleteTaskOperation
import com.kanban.task.FileHandler
import com.kanban.task.GetFileDownloadUrlOperation
import com.kanban.task.GetTaskOperation
import com.kanban.task.ListArchivedTasksOperation
import com.kanban.task.ListBoardBacklogOperation
import com.kanban.task.ListCommentsOperation
import com.kanban.task.ListFilesOperation
import com.kanban.task.ListTasksOperation
import com.kanban.task.MoveTaskOperation
import com.kanban.task.TaskHandler
import com.kanban.task.UpdateCommentOperation
import com.kanban.task.UpdateTaskOperation
import io.mockk.mockk
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Базовый класс для тестов контроллеров задач, комментариев и файлов.
 * Создаёт WebTestClient с прямой привязкой к контроллерам.
 * Моки usecase-операций создаются автоматически и доступны как поля теста.
 *
 * Примечание: для snake_case сериализации используются @JsonProperty аннотации на DTO,
 * что позволяет работать с дефолтным ObjectMapper в тестовом окружении.
 */
internal abstract class BaseTaskControllerTest {
    protected lateinit var createTaskOperation: CreateTaskOperation
    protected lateinit var getTaskOperation: GetTaskOperation
    protected lateinit var listTasksOperation: ListTasksOperation
    protected lateinit var listBoardBacklogOperation: ListBoardBacklogOperation
    protected lateinit var listArchivedTasksOperation: ListArchivedTasksOperation
    protected lateinit var updateTaskOperation: UpdateTaskOperation
    protected lateinit var moveTaskOperation: MoveTaskOperation
    protected lateinit var archiveTaskOperation: ArchiveTaskOperation
    protected lateinit var deleteTaskOperation: DeleteTaskOperation
    protected lateinit var createCommentOperation: CreateCommentOperation
    protected lateinit var updateCommentOperation: UpdateCommentOperation
    protected lateinit var deleteCommentOperation: DeleteCommentOperation
    protected lateinit var listCommentsOperation: ListCommentsOperation
    protected lateinit var attachFileOperation: AttachFileOperation
    protected lateinit var deleteFileOperation: DeleteFileOperation
    protected lateinit var listFilesOperation: ListFilesOperation
    protected lateinit var getFileDownloadUrlOperation: GetFileDownloadUrlOperation

    /**
     * Создаёт WebTestClient привязанный к указанному контроллеру.
     * Моки usecase-операций создаются автоматически и доступны как поля теста.
     *
     * @param controllerClass класс контроллера для тестирования
     * @return настроенный WebTestClient
     */
    @Suppress("LongMethod", "CyclomaticComplexMethod")
    protected fun bindTo(controllerClass: Class<*>): WebTestClient {
        createTaskOperation = mockk()
        getTaskOperation = mockk()
        listTasksOperation = mockk()
        listBoardBacklogOperation = mockk()
        listArchivedTasksOperation = mockk()
        updateTaskOperation = mockk()
        moveTaskOperation = mockk()
        archiveTaskOperation = mockk()
        deleteTaskOperation = mockk()
        createCommentOperation = mockk()
        updateCommentOperation = mockk()
        deleteCommentOperation = mockk()
        listCommentsOperation = mockk()
        attachFileOperation = mockk()
        deleteFileOperation = mockk()
        listFilesOperation = mockk()
        getFileDownloadUrlOperation = mockk()

        val taskHandler =
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
            )

        val commentHandler =
            CommentHandler(
                createCommentOperation = createCommentOperation,
                updateCommentOperation = updateCommentOperation,
                deleteCommentOperation = deleteCommentOperation,
                listCommentsOperation = listCommentsOperation,
            )

        val fileHandler =
            FileHandler(
                attachFileOperation = attachFileOperation,
                deleteFileOperation = deleteFileOperation,
                listFilesOperation = listFilesOperation,
                getFileDownloadUrlOperation = getFileDownloadUrlOperation,
            )

        val controller =
            when (controllerClass) {
                CreateTaskController::class.java -> CreateTaskController(taskHandler)
                GetTaskController::class.java -> GetTaskController(taskHandler)
                ListTasksController::class.java -> ListTasksController(taskHandler)
                ListBoardBacklogController::class.java -> ListBoardBacklogController(taskHandler)
                ListArchivedTasksController::class.java -> ListArchivedTasksController(taskHandler)
                UpdateTaskController::class.java -> UpdateTaskController(taskHandler)
                MoveTaskController::class.java -> MoveTaskController(taskHandler)
                ArchiveTaskController::class.java -> ArchiveTaskController(taskHandler)
                DeleteTaskController::class.java -> DeleteTaskController(taskHandler)
                CreateCommentController::class.java -> CreateCommentController(commentHandler)
                UpdateCommentController::class.java -> UpdateCommentController(commentHandler)
                DeleteCommentController::class.java -> DeleteCommentController(commentHandler)
                ListCommentsController::class.java -> ListCommentsController(commentHandler)
                AttachFileController::class.java -> AttachFileController(fileHandler)
                DeleteFileController::class.java -> DeleteFileController(fileHandler)
                ListFilesController::class.java -> ListFilesController(fileHandler)
                GetFileDownloadUrlController::class.java -> GetFileDownloadUrlController(fileHandler)
                else -> throw IllegalArgumentException("Unsupported controller: $controllerClass")
            }

        return WebTestClient.bindToController(controller).build()
    }
}
