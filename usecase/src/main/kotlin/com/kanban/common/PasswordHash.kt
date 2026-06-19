package com.kanban.common

/**
 * Value-объект, представляющий хеш пароля пользователя.
 * Хранит результат хеширования пароля в виде строки.
 */
data class PasswordHash(
    val value: String,
)
