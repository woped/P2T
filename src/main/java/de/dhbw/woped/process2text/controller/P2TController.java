package de.dhbw.woped.process2text.controller;

import de.dhbw.woped.process2text.service.P2TLLMService;
import de.dhbw.woped.process2text.service.P2TService;
import de.dhbw.woped.process2text.service.RagService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST controller for Process-to-Text (P2T) translation.
 *
 * <p>This controller provides the main API endpoints for converting business process
 * models into natural language text descriptions. It supports both traditional
 * algorithmic text generation and modern LLM-based approaches.
 *
 * <h2>Endpoints</h2>
 * <table border="1">
 *   <tr><th>Method</th><th>Path</th><th>Description</th></tr>
 *   <tr><td>POST</td><td>/generateText</td><td>Algorithmic text generation</td></tr>
 *   <tr><td>POST</td><td>/generateTextLLM</td><td>LLM-based text generation</td></tr>
 *   <tr><td>GET</td><td>/gptModels</td><td>List available models for a provider</td></tr>
 *   <tr><td>GET</td><td>/providers</td><td>List supported LLM providers</td></tr>
 * </table>
 *
 * <h2>Supported Input Formats</h2>
 * <ul>
 *   <li>PNML (Petri Net Markup Language)</li>
 *   <li>BPMN 2.0 (Business Process Model and Notation)</li>
 * </ul>
 *
 * <h2>LLM Providers</h2>
 * <ul>
 *   <li><b>OpenAI:</b> GPT-4, GPT-3.5-turbo (requires API key)</li>
 *   <li><b>Gemini:</b> Google's Gemini models (requires API key)</li>
 *   <li><b>LM Studio:</b> Local models via LM Studio (no API key required)</li>
 * </ul>
 *
 * @author WoPeD Team
 * @see P2TService
 * @see P2TLLMService
 * @see RagService
 */
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
@RestController
public class P2TController {

    private static final Logger logger = LoggerFactory.getLogger(P2TController.class);

    private final P2TService p2tService;
    private final P2TLLMService llmService;
    private final RagService ragService;
    private final Counter httpRequestsTotal;
    private final Timer httpRequestDuration;

    /**
     * Constructs a new P2TController with all required dependencies.
     *
     * @param p2tService          the algorithmic text generation service
     * @param llmService          the LLM-based text generation service
     * @param ragService          the RAG service for prompt enrichment
     * @param httpRequestsTotal   counter for tracking total HTTP requests
     * @param httpRequestDuration timer for measuring request processing time
     */
    @Autowired
    public P2TController(
            P2TService p2tService,
            P2TLLMService llmService,
            RagService ragService,
            @Qualifier("httpRequestsTotal") Counter httpRequestsTotal,
            @Qualifier("httpRequestDuration") Timer httpRequestDuration) {
        this.p2tService = p2tService;
        this.llmService = llmService;
        this.ragService = ragService;
        this.httpRequestsTotal = httpRequestsTotal;
        this.httpRequestDuration = httpRequestDuration;
    }

    /**
     * Generates text from a process model using the traditional algorithmic approach.
     *
     * <p>This endpoint uses rule-based NLP techniques to generate text descriptions
     * without requiring external API calls. It's suitable for offline use and provides
     * consistent, deterministic output.
     *
     * @param body the process model in PNML or BPMN XML format
     * @return natural language text describing the process
     * @throws de.dhbw.woped.process2text.exception.InvalidInputException if the input format is invalid
     */
    @PostMapping(value = "/generateText", consumes = "text/plain", produces = "text/plain")
    public String generateText(@RequestBody String body) {
        httpRequestsTotal.increment();
        return httpRequestDuration.record(() -> {
            logger.debug("Received generateText request, body length: {}", body.length());
            String response = p2tService.generateText(body);
            logger.debug("Generated text, response length: {}", response.length());
            return response;
        });
    }

    /**
     * Generates text from a process model using a Large Language Model (LLM).
     *
     * <p>This endpoint leverages external LLM APIs to generate more natural and
     * context-aware text descriptions. It supports optional RAG (Retrieval-Augmented
     * Generation) for enhanced prompt context.
     *
     * <h3>Provider Requirements</h3>
     * <ul>
     *   <li><b>openai:</b> Requires valid API key</li>
     *   <li><b>gemini:</b> Requires valid API key</li>
     *   <li><b>lmstudio:</b> No API key required (local server)</li>
     * </ul>
     *
     * @param body     the process model in PNML or BPMN XML format
     * @param apiKey   API key for the LLM provider (required for openai/gemini, optional for lmstudio)
     * @param prompt   the instruction prompt to guide text generation style and content
     * @param gptModel the specific model identifier to use (e.g., "gpt-4", "gemini-pro")
     * @param provider the LLM provider name: "openai", "gemini", or "lmstudio"
     * @param useRag   if true, enriches the prompt with domain context via RAG service
     * @return natural language text describing the process
     * @throws ResponseStatusException with HTTP 400 if provider is invalid or API key is missing
     * @throws de.dhbw.woped.process2text.service.llm.LlmException if LLM API call fails
     */
    @PostMapping(value = "/generateTextLLM", consumes = "text/plain", produces = "text/plain")
    public String generateTextLLM(
            @RequestBody String body,
            @RequestParam(required = false) String apiKey,
            @RequestParam String prompt,
            @RequestParam String gptModel,
            @RequestParam String provider,
            @RequestParam(defaultValue = "false") boolean useRag) {

        logger.info("LLM request: provider={}, model={}, useRag={}", provider, gptModel, useRag);

        // Validate provider
        if (!llmService.getSupportedProviders().contains(provider.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid provider. Supported: " + llmService.getSupportedProviders());
        }

        // Validate API key for providers that require it
        if (llmService.providerRequiresApiKey(provider) && isBlank(apiKey)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "API key is required for provider: " + provider);
        }

        // Enrich prompt with RAG if enabled
        String enrichedPrompt = prompt;
        if (useRag) {
            enrichedPrompt = ragService.enrichPrompt(prompt, body);
            logger.info("RAG enrichment: original={} chars, enriched={} chars",
                    prompt.length(), enrichedPrompt.length());
        }

        return llmService.generateText(body, provider, apiKey, gptModel, enrichedPrompt);
    }

    /**
     * Retrieves the list of available models for a given LLM provider.
     *
     * <p>Queries the provider's API to fetch currently available models.
     * For OpenAI and Gemini, an API key is required. For LM Studio, this
     * returns the models currently loaded in the local server.
     *
     * @param apiKey   API key for authentication (required for openai/gemini, ignored for lmstudio)
     * @param provider the LLM provider name: "openai", "gemini", or "lmstudio"
     * @return list of available model identifiers (e.g., ["gpt-4", "gpt-3.5-turbo"])
     * @throws ResponseStatusException with HTTP 400 if provider is invalid
     */
    @GetMapping("/gptModels")
    public List<String> getModels(
            @RequestParam(required = false) String apiKey,
            @RequestParam String provider) {

        logger.info("Fetching models for provider: {}", provider);

        return switch (provider.toLowerCase()) {
            case "openai" -> llmService.getAvailableModels("openai", apiKey);
            case "gemini" -> llmService.getAvailableModels("gemini", apiKey);
            case "lmstudio" -> llmService.getAvailableModels("lmstudio", null);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid provider. Supported: openai, gemini, lmstudio");
        };
    }

    /**
     * Returns the list of all supported LLM providers.
     *
     * @return list of provider names (e.g., ["openai", "gemini", "lmstudio"])
     */
    @GetMapping("/providers")
    public List<String> getProviders() {
        return llmService.getSupportedProviders();
    }

    /**
     * Checks if a string is null, empty, or contains only whitespace.
     *
     * @param str the string to check
     * @return true if the string is blank, false otherwise
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
