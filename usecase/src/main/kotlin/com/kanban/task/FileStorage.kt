package com.kanban.task

import kotlin.time.Duration

/**
 * Порт хранилища файлов (MinIO, S3 и т. п.).
 * Инкапсулирует операции загрузки, удаления и выдачи presigned-URL для скачивания.
 */
interface FileStorage {
    /**
     * Загружает содержимое в хранилище по указанному ключу.
     *
     * @param key ключ (путь) объекта в хранилище
     * @param content содержимое файла
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
