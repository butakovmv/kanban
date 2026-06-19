package com.kanban.common

data class Email(
    val value: String,
) {
    init {
        require(value.length <= 254) { "Email must not exceed 254 characters" }
        require(value.contains("@")) { "Email must contain @" }
    }
}
