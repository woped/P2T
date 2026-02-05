package de.dhbw.woped.process2text.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Centralized configuration for external service URLs used by the P2T service.
 *
 * <p>This configuration class manages connection URLs for:
 * <ul>
 *   <li>Internal microservices (RAG, Transformer)</li>
 *   <li>External LLM APIs (OpenAI, Google Gemini, LM Studio)</li>
 * </ul>
 *
 * <h2>Configuration Sources (in order of priority)</h2>
 * <ol>
 *   <li>Java System Properties (e.g., {@code -Drag.service.url=...})</li>
 *   <li>Environment Variables (e.g., {@code RAG_SERVICE_URL=...})</li>
 *   <li>application.properties file</li>
 *   <li>Default values defined in this class</li>
 * </ol>
 *
 * <h2>Docker/Production Usage</h2>
 * <p>In containerized environments, configure via environment variables in docker-compose.yml
 * or Kubernetes manifests. Service names should use Docker network hostnames
 * (e.g., {@code http://rag:5000} instead of {@code http://localhost:5000}).
 *
 * @author WoPeD Team
 * @see de.dhbw.woped.process2text.service.RagService
 * @see de.dhbw.woped.process2text.service.TransformerService
 * @see de.dhbw.woped.process2text.service.llm.LlmProvider
 */
@Configuration
public class ServiceConfig {

    private static final Logger logger = LoggerFactory.getLogger(ServiceConfig.class);

    // =========================================================================
    // Internal Service URLs
    // =========================================================================

    /**
     * URL of the RAG (Retrieval-Augmented Generation) service.
     * Used for enriching prompts with domain-specific context.
     */
    @Value("${rag.service.url:${RAG_SERVICE_URL:http://localhost:5000}}")
    private String ragServiceUrl;

    /**
     * URL of the Model Transformer service.
     * Used for converting between PNML and BPMN formats.
     */
    @Value("${transformer.service.url:${TRANSFORMER_SERVICE_URL:http://localhost:5001}}")
    private String transformerServiceUrl;

    // =========================================================================
    // LLM Provider URLs
    // =========================================================================

    /**
     * Base URL for OpenAI API.
     * Default: https://api.openai.com/v1
     */
    @Value("${openai.api.url:${OPENAI_API_URL:https://api.openai.com/v1}}")
    private String openaiApiUrl;

    /**
     * Base URL for Google Gemini API.
     * Default: https://generativelanguage.googleapis.com
     */
    @Value("${gemini.api.url:${GEMINI_API_URL:https://generativelanguage.googleapis.com}}")
    private String geminiApiUrl;

    /**
     * Base URL for LM Studio local server.
     * Default: http://localhost:1234
     */
    @Value("${lmstudio.api.url:${LMSTUDIO_API_URL:http://localhost:1234}}")
    private String lmstudioApiUrl;

    // =========================================================================
    // Getters
    // =========================================================================

    /**
     * Returns the configured RAG service URL.
     *
     * @return the RAG service base URL
     */
    public String getRagServiceUrl() {
        return ragServiceUrl;
    }

    /**
     * Returns the configured Transformer service URL.
     *
     * @return the Transformer service base URL
     */
    public String getTransformerServiceUrl() {
        return transformerServiceUrl;
    }

    /**
     * Returns the configured OpenAI API URL.
     *
     * @return the OpenAI API base URL
     */
    public String getOpenaiApiUrl() {
        return openaiApiUrl;
    }

    /**
     * Returns the configured Google Gemini API URL.
     *
     * @return the Gemini API base URL
     */
    public String getGeminiApiUrl() {
        return geminiApiUrl;
    }

    /**
     * Returns the configured LM Studio API URL.
     *
     * @return the LM Studio API base URL
     */
    public String getLmstudioApiUrl() {
        return lmstudioApiUrl;
    }

    // =========================================================================
    // Initialization
    // =========================================================================

    /**
     * Logs the current configuration on application startup.
     * This helps with debugging configuration issues in different environments.
     */
    @PostConstruct
    public void logConfiguration() {
        logger.info("=== P2T Service Configuration ===");
        logger.info("RAG Service URL: {}", ragServiceUrl);
        logger.info("Transformer Service URL: {}", transformerServiceUrl);
        logger.info("OpenAI API URL: {}", openaiApiUrl);
        logger.info("Gemini API URL: {}", geminiApiUrl);
        logger.info("LM Studio API URL: {}", lmstudioApiUrl);
        logger.info("=================================");
    }
}
