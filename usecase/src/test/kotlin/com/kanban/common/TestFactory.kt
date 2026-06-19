package com.kanban.common

import java.time.Instant
import java.util.UUID

internal object TestFactory {
    fun uuid(): String = UUID.randomUUID().toString()

    fun now(): Instant = Instant.now()

    fun email(): String = "user-${uuid().take(8)}@kanban.test"

    fun string(prefix: String = "test"): String = "$prefix-${uuid().take(8)}"

    fun positiveInt(max: Int = 999): Int = (1..max).random()
}
