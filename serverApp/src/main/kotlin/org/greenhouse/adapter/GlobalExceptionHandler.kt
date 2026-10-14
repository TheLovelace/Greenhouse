package org.greenhouse.adapter

import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.dao.DataAccessException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.NoHandlerFoundException
import jakarta.persistence.PersistenceException

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<Map<String, String>> {
        val errors = e.bindingResult.fieldErrors
            .joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity
            .badRequest()
            .body(mapOf("error" to errors))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleInvalidRequestBody(): ResponseEntity<Map<String, String>> =
        ResponseEntity
            .badRequest()
            .body(mapOf("error" to "Corpo da requisição inválido ou campos obrigatórios ausentes"))

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

    @ExceptionHandler(DataAccessException::class)
    fun handleDatabaseException(e: DataAccessException): ResponseEntity<Map<String, String?>> =
        handleAppException(DatabaseConnectionException(cause = e))

    @ExceptionHandler(PersistenceException::class)
    fun handlePersistenceException(e: PersistenceException): ResponseEntity<Map<String, String?>> =
        handleAppException(DatabaseConnectionException(cause = e))
}
