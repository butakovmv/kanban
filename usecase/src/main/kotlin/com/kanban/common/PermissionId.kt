package com.kanban.common

/**
 * Value-объект, представляющий уникальный идентификатор разрешения на доступ к ресурсу.
 * Хранит строковое значение UUID.
 */
data class PermissionId(
    val value: String,
)
