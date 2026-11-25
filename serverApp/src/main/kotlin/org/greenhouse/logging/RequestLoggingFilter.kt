package org.greenhouse.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import java.nio.charset.Charset
import java.util.UUID

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
class RequestLoggingFilter(
    private val masker: SensitiveDataMasker,
) : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(RequestLoggingFilter::class.java)

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI.removePrefix(request.contextPath)
        return IGNORED_PREFIXES.any { path.startsWith(it) }
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val requestId =
            request
                .getHeader(REQUEST_ID_HEADER)
                ?.takeIf { it.isNotBlank() && it.length <= 64 && REQUEST_ID_PATTERN.matches(it) }
                ?: UUID.randomUUID().toString().substring(0, 8)

        MDC.put(MDC_REQUEST_ID, requestId)
        response.setHeader(REQUEST_ID_HEADER, requestId)

        val wrappedRequest = ContentCachingRequestWrapper(request, MAX_BODY_BYTES)
        val start = System.nanoTime()
        var failure: Throwable? = null

        try {
            filterChain.doFilter(wrappedRequest, response)
        } catch (e: Throwable) {
            failure = e
            throw e
        } finally {
            val elapsedMs = (System.nanoTime() - start) / 1_000_000
            logRequest(wrappedRequest, response, elapsedMs, failure)
            MDC.remove(MDC_REQUEST_ID)
        }
    }

    private fun logRequest(
        request: ContentCachingRequestWrapper,
        response: HttpServletResponse,
        elapsedMs: Long,
        failure: Throwable?,
    ) {
        val query = request.queryString?.let { "?" + masker.maskQueryString(it) } ?: ""
        val route = "${request.method} ${request.requestURI}$query"

        if (failure != null) {
            log.error("{} -> exceção não tratada após {} ms", route, elapsedMs, failure)
            return
        }

        val status = response.status
        val message = "{} -> {} ({} ms)"
        when {
            status >= 500 -> log.error(message, route, status, elapsedMs)
            status >= 400 -> log.warn(message, route, status, elapsedMs)
            else -> log.info(message, route, status, elapsedMs)
        }

        if (log.isDebugEnabled) {
            body(request)?.let { log.debug("{} body: {}", route, it) }
        }
    }

    private fun body(request: ContentCachingRequestWrapper): String? {
        val bytes = request.contentAsByteArray
        if (bytes.isEmpty()) return null

        val contentType = request.contentType?.lowercase() ?: ""
        if (!contentType.contains("json")) {
            return "<${bytes.size} bytes de $contentType omitidos>"
        }

        val charset =
            request.characterEncoding?.let { runCatching { Charset.forName(it) }.getOrNull() }
                ?: Charsets.UTF_8
        val masked = masker.maskJson(String(bytes, charset))
        val truncated = request.contentLengthLong > bytes.size
        return if (truncated) "$masked... (truncado)" else masked
    }

    companion object {
        const val REQUEST_ID_HEADER = "X-Request-Id"
        const val MDC_REQUEST_ID = "requestId"
        private const val MAX_BODY_BYTES = 10 * 1024
        private val REQUEST_ID_PATTERN = Regex("^[A-Za-z0-9._-]+$")

        private val IGNORED_PREFIXES =
            listOf(
                "/swagger-ui",
                "/v3/api-docs",
                "/api/v3/api-docs",
                "/openapi",
                "/api/openapi",
            )
    }
}
