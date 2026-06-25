package com.kanban.logging

import ch.qos.logback.classic.pattern.ThrowableProxyConverter
import ch.qos.logback.classic.spi.IThrowableProxy

class CleanedThrowableConverter : ThrowableProxyConverter() {
    private val allowedPackages =
        listOf(
            "com.kanban",
            "java.base",
            "java.lang",
            "java.util",
            "org.springframework",
            "kotlin.",
        )

    override fun throwableProxyToString(tp: IThrowableProxy): String {
        val full = super.throwableProxyToString(tp)
        return full
            .lines()
            .filter { line ->
                val trimmed = line.trimStart()
                when {
                    trimmed.startsWith("at ") -> allowedPackages.any { trimmed.startsWith("at $it") }
                    trimmed.contains("reactor.") -> false
                    trimmed.startsWith("*__checkpoint") -> false
                    trimmed.startsWith("Error has been observed") -> false
                    trimmed == "Original Stack Trace:" -> false
                    else -> true
                }
            }.joinToString("\n")
            .trimEnd()
    }
}
