package com.kanban.task

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Реализация операции получения presigned-URL для скачивания прикреплённого файла.
 * Находит прикрепление по ID и запрашивает у [FileStorage] URL с ограниченным сроком действия.
 */
internal class GetFileDownloadUrlOperationImpl
    @JvmOverloads
    constructor(
        private val fileAttachmentRepository: FileAttachmentRepository,
        private val fileStorage: FileStorage,
        private val expiresIn: Duration = DEFAULT_EXPIRES_IN,
    ) : GetFileDownloadUrlOperation {
        override suspend fun execute(arg: GetFileDownloadUrlOperation.Arg): GetFileDownloadUrlOperation.Result {
            val file =
                fileAttachmentRepository.findById(arg.fileId) ?: return GetFileDownloadUrlOperation.Result.NotFound
            val url = fileStorage.getDownloadUrl(file.storageKey, expiresIn)
            return GetFileDownloadUrlOperation.Result.Success(url)
        }

        companion object {
            private val DEFAULT_EXPIRES_IN: Duration = 15.minutes
        }
    }
