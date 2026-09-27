package org.greenhouse.adapter

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.NoHandlerFoundException

@RestControllerAdvice
class GlobalExceptionHandler {

    /** Intercepta rotas inexistentes (nenhum @RequestMapping correspondente). */
    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFound(e: NoHandlerFoundException): ResponseEntity<Map<String, String?>> {
        val notFound = NotFoundException("Rota não encontrada: ${e.httpMethod} ${e.requestURL}")
        return ResponseEntity
            .status(notFound.statusCode)
            .body(mapOf("error" to notFound.message))
    }

    /** Intercepta qualquer exceção de domínio (DatabaseConnectionException, AuthenticationException, etc). */
    @ExceptionHandler(AppException::class)
    fun handleAppException(e: AppException): ResponseEntity<Map<String, String?>> =
        ResponseEntity
            .status(e.statusCode)
            .body(mapOf("error" to e.message))
}