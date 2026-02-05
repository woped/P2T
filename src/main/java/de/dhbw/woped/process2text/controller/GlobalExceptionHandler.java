package de.dhbw.woped.process2text.controller;

import de.dhbw.woped.process2text.service.llm.LlmException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Global exception handler for all REST controllers in the P2T application.
 *
 * <p>This class provides centralized exception handling using Spring's
 * {@link RestControllerAdvice} mechanism. It ensures consistent JSON error
 * responses across all API endpoints.
 *
 * <h2>Error Response Format</h2>
 * <p>All error responses follow a standard JSON structure:
 * <pre>{@code
 * {
 *   "timestamp": "2024-01-15T10:30:00.000Z",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Descriptive error message",
 *   "details": { ... }  // optional, context-specific details
 * }
 * }</pre>
 *
 * <h2>Handled Exceptions</h2>
 * <ul>
 *   <li>{@link LlmException} - LLM-specific errors (authentication, rate limits, etc.)</li>
 *   <li>{@link ResponseStatusException} - Explicit HTTP status exceptions</li>
 *   <li>{@link MissingServletRequestParameterException} - Missing required parameters</li>
 *   <li>{@link IllegalArgumentException} - Invalid argument values</li>
 *   <li>{@link Exception} - Catch-all for unexpected errors</li>
 * </ul>
 *
 * @author WoPeD Team
 * @see LlmException
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles exceptions thrown by LLM operations.
     *
     * <p>Maps LLM-specific error types to appropriate HTTP status codes
     * and includes provider and error type information in the response.
     *
     * @param ex the LLM exception
     * @return ResponseEntity with error details and appropriate HTTP status
     */
    @ExceptionHandler(LlmException.class)
    public ResponseEntity<Map<String, Object>> handleLlmException(LlmException ex) {
        logger.error("LLM exception: {}", ex.toString());

        Map<String, Object> body = createErrorBody(
                ex.getHttpStatus(),
                ex.getMessage(),
                Map.of(
                        "provider", ex.getProvider(),
                        "errorType", ex.getErrorType().name()
                )
        );

        return ResponseEntity.status(ex.getHttpStatus()).body(body);
    }

    /**
     * Handles {@link ResponseStatusException} thrown by controllers.
     *
     * <p>Preserves the HTTP status code and reason phrase from the exception.
     *
     * @param ex the response status exception
     * @return ResponseEntity with the specified status and reason
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        logger.warn("Response status exception: {} - {}", ex.getStatusCode(), ex.getReason());

        Map<String, Object> body = createErrorBody(
                HttpStatus.valueOf(ex.getStatusCode().value()),
                ex.getReason(),
                null
        );

        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    /**
     * Handles missing required request parameters.
     *
     * <p>Returns HTTP 400 Bad Request with the name of the missing parameter.
     *
     * @param ex the missing parameter exception
     * @return ResponseEntity with HTTP 400 and parameter name
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParams(MissingServletRequestParameterException ex) {
        logger.warn("Missing parameter: {}", ex.getParameterName());

        Map<String, Object> body = createErrorBody(
                HttpStatus.BAD_REQUEST,
                "Missing required parameter: " + ex.getParameterName(),
                Map.of("parameterName", ex.getParameterName())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Handles invalid argument exceptions.
     *
     * <p>Typically thrown when input validation fails at the service layer.
     *
     * @param ex the illegal argument exception
     * @return ResponseEntity with HTTP 400 and validation message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn("Illegal argument: {}", ex.getMessage());

        Map<String, Object> body = createErrorBody(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Catch-all handler for any uncaught exceptions.
     *
     * <p>Returns a generic error message to avoid leaking implementation details.
     * The full exception is logged for debugging purposes.
     *
     * @param ex the unexpected exception
     * @return ResponseEntity with HTTP 500 and generic error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        logger.error("Unexpected exception", ex);

        Map<String, Object> body = createErrorBody(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    /**
     * Creates a standardized error response body.
     *
     * @param status  the HTTP status code
     * @param message the error message
     * @param details optional additional details (may be null)
     * @return a map containing the error response fields
     */
    private Map<String, Object> createErrorBody(HttpStatus status, String message, Map<String, Object> details) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        if (details != null && !details.isEmpty()) {
            body.put("details", details);
        }

        return body;
    }
}
