package com.kanban.http

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import java.net.URI

@RestControllerAdvice
class GlobalErrorHandler {

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidation(ex: WebExchangeBindException): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        detail.title = "Validation Error"
        detail.detail = ex.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        detail.type = URI.create("/errors/validation")
        return detail
    }

    @ExceptionHandler(Throwable::class)
    fun handleGeneral(ex: Throwable): ProblemDetail {
        val detail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        detail.title = "Internal Error"
        detail.detail = ex.message ?: "Unknown error"
        detail.type = URI.create("/errors/internal")
        return detail
    }
}
