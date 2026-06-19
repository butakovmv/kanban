package com.kanban.http.document

import com.kanban.document.CreateDocumentOperation
import com.kanban.document.DeleteDocumentOperation
import com.kanban.document.DocumentHandler
import com.kanban.document.DocumentStorage
import com.kanban.document.GetDocumentOperation
import com.kanban.document.ListDocumentsOperation
import com.kanban.document.ReplaceDocumentOperation
import com.kanban.document.UpdateDocumentOperation
import io.mockk.mockk
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Базовый класс для тестов контроллеров документов.
 * Создаёт WebTestClient с прямой привязкой к контроллерам.
 * Моки usecase-операций и порта хранилища создаются автоматически и доступны как поля теста.
 *
 * Примечание: для snake_case сериализации используются @JsonProperty аннотации на DTO,
 * что позволяет работать с дефолтным ObjectMapper в тестовом окружении.
 */
internal abstract class BaseDocumentControllerTest {
    protected lateinit var createDocumentOperation: CreateDocumentOperation
    protected lateinit var getDocumentOperation: GetDocumentOperation
    protected lateinit var listDocumentsOperation: ListDocumentsOperation
    protected lateinit var updateDocumentOperation: UpdateDocumentOperation
    protected lateinit var replaceDocumentOperation: ReplaceDocumentOperation
    protected lateinit var deleteDocumentOperation: DeleteDocumentOperation
    protected lateinit var documentStorage: DocumentStorage

    /**
     * Создаёт WebTestClient привязанный к указанному контроллеру.
     * Моки usecase-операций и порта хранилища создаются автоматически и доступны как поля теста.
     *
     * @param controllerClass класс контроллера для тестирования
     * @return настроенный WebTestClient
     */
    @Suppress("LongMethod", "CyclomaticComplexMethod")
    protected fun bindTo(controllerClass: Class<*>): WebTestClient {
        createDocumentOperation = mockk()
        getDocumentOperation = mockk()
        listDocumentsOperation = mockk()
        updateDocumentOperation = mockk()
        replaceDocumentOperation = mockk()
        deleteDocumentOperation = mockk()
        documentStorage = mockk()

        val documentHandler =
            DocumentHandler(
                createDocumentOperation = createDocumentOperation,
                getDocumentOperation = getDocumentOperation,
                listDocumentsOperation = listDocumentsOperation,
                updateDocumentOperation = updateDocumentOperation,
                replaceDocumentOperation = replaceDocumentOperation,
                deleteDocumentOperation = deleteDocumentOperation,
                documentStorage = documentStorage,
            )

        val controller =
            when (controllerClass) {
                CreateDocumentController::class.java -> CreateDocumentController(documentHandler)
                GetDocumentController::class.java -> GetDocumentController(documentHandler)
                ListDocumentsController::class.java -> ListDocumentsController(documentHandler)
                UpdateDocumentController::class.java -> UpdateDocumentController(documentHandler)
                ReplaceDocumentController::class.java -> ReplaceDocumentController(documentHandler)
                DeleteDocumentController::class.java -> DeleteDocumentController(documentHandler)
                DownloadDocumentController::class.java -> DownloadDocumentController(documentHandler)
                else -> throw IllegalArgumentException("Unsupported controller: $controllerClass")
            }

        return WebTestClient.bindToController(controller).build()
    }
}
