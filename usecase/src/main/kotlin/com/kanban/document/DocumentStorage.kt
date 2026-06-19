package com.kanban.document

import kotlin.time.Duration

/**
 * Порт хранилища документов (MinIO, S3 и т. п.).
 * Инкапсулирует операции загрузки, удаления и выдачи presigned-URL для скачивания содержимого документов.
 */
interface DocumentStorage {
    /**
     * Загружает содержимое документа в хранилище по указанному ключу.
     *
     * @param key ключ (путь) объекта в хранилище
     * @param content содержимое документа
     * @param contentType MIME-тип содержимого
     * @return фактически использованный ключ объекта (или его URL)
     */
    suspend fun upload(
        key: String,
        content: ByteArray,
        contentType: String,
    ): String

    /**
     * Удаляет объект из хранилища по ключу.
     *
     * @param key ключ (путь) объекта в хранилище
     */
    suspend fun delete(key: String)

    /**
     * Генерирует presigned-URL для скачивания объекта с ограниченным сроком действия.
     *
     * @param key ключ (путь) объекта в хранилище
     * @param expiresIn срок действия URL
     * @return presigned-URL
     */
    suspend fun getDownloadUrl(
        key: String,
        expiresIn: Duration,
    ): String
}
