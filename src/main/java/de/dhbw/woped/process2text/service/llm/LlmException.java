package de.dhbw.woped.process2text.service.llm;

import org.springframework.http.HttpStatus;

/**
 * Custom runtime exception for LLM-related errors.
 *
 * <p>This exception provides structured error information that can be used
 * to generate meaningful API responses. It includes:
 * <ul>
 *   <li>An {@link ErrorType} categorizing the error</li>
 *   <li>The provider name where the error occurred</li>
 *   <li>An appropriate HTTP status code for REST responses</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * if (apiKey == null) {
 *     throw new LlmException(ErrorType.MISSING_API_KEY, "openai",
 *         "API key is required for OpenAI");
 * }
 * }</pre>
 *
 * <h2>Error Handling</h2>
 * <p>This exception is caught by {@link de.dhbw.woped.process2text.controller.GlobalExceptionHandler}
 * and converted to a standardized JSON error response.
 *
 * @author WoPeD Team
 * @see ErrorType
 * @see de.dhbw.woped.process2text.controller.GlobalExceptionHandler#handleLlmException
 */
public class LlmException extends RuntimeException {

    private final ErrorType errorType;
    private final String provider;
    private final HttpStatus httpStatus;

    /**
     * Creates a new LlmException with the specified error type, provider, and message.
     *
     * @param errorType the type of error that occurred
     * @param provider  the name of the LLM provider where the error occurred
     * @param message   a descriptive error message
     */
    public LlmException(ErrorType errorType, String provider, String message) {
        super(message);
        this.errorType = errorType;
        this.provider = provider;
        this.httpStatus = errorType.getDefaultHttpStatus();
    }

    /**
     * Creates a new LlmException with an underlying cause.
     *
     * @param errorType the type of error that occurred
     * @param provider  the name of the LLM provider where the error occurred
     * @param message   a descriptive error message
     * @param cause     the underlying exception that caused this error
     */
    public LlmException(ErrorType errorType, String provider, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.provider = provider;
        this.httpStatus = errorType.getDefaultHttpStatus();
    }

    /**
     * Returns the error type classification.
     *
     * @return the error type
     */
    public ErrorType getErrorType() {
        return errorType;
    }

    /**
     * Returns the name of the provider where the error occurred.
     *
     * @return the provider name
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Returns the appropriate HTTP status code for this error.
     *
     * @return the HTTP status code
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    /**
     * Enumeration of possible LLM error types.
     *
     * <p>Each error type has a default message and an associated HTTP status code
     * for REST API responses.
     */
    public enum ErrorType {
        /** API key was not provided but is required */
        MISSING_API_KEY("API key is required", HttpStatus.BAD_REQUEST),

        /** Model was not specified in the request */
        MISSING_MODEL("Model selection is required", HttpStatus.BAD_REQUEST),

        /** Request parameters are invalid or malformed */
        INVALID_REQUEST("Invalid request parameters", HttpStatus.BAD_REQUEST),

        /** API key is invalid or expired */
        AUTHENTICATION_FAILED("Authentication failed - check API key", HttpStatus.UNAUTHORIZED),

        /** Provider rate limit has been exceeded */
        RATE_LIMITED("Rate limit exceeded - please wait", HttpStatus.TOO_MANY_REQUESTS),

        /** The specified model does not exist or is not accessible */
        MODEL_NOT_FOUND("Requested model not found", HttpStatus.NOT_FOUND),

        /** The LLM provider service is unavailable */
        PROVIDER_UNAVAILABLE("Provider service unavailable", HttpStatus.SERVICE_UNAVAILABLE),

        /** Provider returned an empty or null response */
        EMPTY_RESPONSE("Provider returned empty response", HttpStatus.NO_CONTENT),

        /** Failed to parse the provider's response */
        PARSE_ERROR("Failed to parse provider response", HttpStatus.INTERNAL_SERVER_ERROR),

        /** Network connectivity error */
        NETWORK_ERROR("Network error connecting to provider", HttpStatus.BAD_GATEWAY),

        /** Any other unexpected error */
        UNKNOWN_ERROR("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);

        private final String defaultMessage;
        private final HttpStatus defaultHttpStatus;

        /**
         * Creates an error type with default message and HTTP status.
         *
         * @param defaultMessage    the default error message
         * @param defaultHttpStatus the default HTTP status code
         */
        ErrorType(String defaultMessage, HttpStatus defaultHttpStatus) {
            this.defaultMessage = defaultMessage;
            this.defaultHttpStatus = defaultHttpStatus;
        }

        /**
         * Returns the default message for this error type.
         *
         * @return the default error message
         */
        public String getDefaultMessage() {
            return defaultMessage;
        }

        /**
         * Returns the default HTTP status code for this error type.
         *
         * @return the HTTP status code
         */
        public HttpStatus getDefaultHttpStatus() {
            return defaultHttpStatus;
        }
    }

    /**
     * Returns a string representation of this exception for logging.
     *
     * @return formatted string with error type, provider, and message
     */
    @Override
    public String toString() {
        return String.format("LlmException{type=%s, provider='%s', message='%s'}",
                errorType, provider, getMessage());
    }
}
