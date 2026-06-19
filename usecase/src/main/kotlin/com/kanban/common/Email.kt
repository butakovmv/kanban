package com.kanban.common

/**
 * Value-объект, представляющий email-адрес пользователя.
 * Валидирует длину (до 254 символов) и наличие символа '@'.
 */
data class Email(
    val value: String,
) {
    init {
        require(value.length <= 254) { "Email must not exceed 254 characters" }
        require(value.contains("@")) { "Email must contain @" }
    }
}
