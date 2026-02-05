package de.dhbw.woped.process2text.service;

import de.dhbw.woped.process2text.service.llm.LlmException;
import de.dhbw.woped.process2text.service.llm.LlmProvider;
import de.dhbw.woped.process2text.service.llm.LlmProviderFactory;
import de.dhbw.woped.process2text.service.llm.dto.LlmRequest;
import de.dhbw.woped.process2text.service.llm.dto.LlmResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service for LLM-based process-to-text generation.
 *
 * <p>This service acts as a facade that orchestrates the entire LLM text generation
 * workflow. It handles:
 * <ul>
 *   <li>Process model format detection and transformation</li>
 *   <li>Provider selection and validation</li>
 *   <li>Request building and API communication</li>
 *   <li>Error handling and response processing</li>
 * </ul>
 *
 * <h2>Architecture</h2>
 * <p>This service implements the Facade pattern, delegating actual LLM API calls
 * to provider-specific implementations via the {@link LlmProviderFactory}. This
 * allows easy addition of new LLM providers without modifying this class.
 *
 * <h2>Process Model Preparation</h2>
 * <p>PNML models are automatically converted to BPMN before sending to the LLM,
 * as LLMs generally produce better results with BPMN's more descriptive format.
 *
 * <h2>Supported Providers</h2>
 * <ul>
 *   <li><b>OpenAI:</b> GPT-4, GPT-3.5-turbo, etc.</li>
 *   <li><b>Gemini:</b> Google's Gemini models</li>
 *   <li><b>LM Studio:</b> Local models via LM Studio server</li>
 * </ul>
 *
 * @author WoPeD Team
 * @see LlmProviderFactory
 * @see LlmProvider
 * @see TransformerService
 */
@Service
public class P2TLLMService {

    private static final Logger logger = LoggerFactory.getLogger(P2TLLMService.class);

    private final LlmProviderFactory providerFactory;
    private final TransformerService transformerService;

    /**
     * Constructs a new P2TLLMService with required dependencies.
     *
     * @param providerFactory    factory for obtaining LLM provider instances
     * @param transformerService service for converting between PNML and BPMN formats
     */
    public P2TLLMService(LlmProviderFactory providerFactory, TransformerService transformerService) {
        this.providerFactory = providerFactory;
        this.transformerService = transformerService;
    }

    /**
     * Generates a natural language description from a process model using an LLM.
     *
     * <p>The method performs the following steps:
     * <ol>
     *   <li>Detects the input format (PNML or BPMN)</li>
     *   <li>Converts PNML to BPMN if needed for better LLM processing</li>
     *   <li>Builds an LLM request with the provided parameters</li>
     *   <li>Sends the request to the specified provider</li>
     *   <li>Returns the generated text from the response</li>
     * </ol>
     *
     * @param processModel the process model in PNML or BPMN XML format
     * @param providerName the LLM provider name: "openai", "gemini", or "lmstudio"
     * @param apiKey       the API key for authentication (may be null for LM Studio)
     * @param model        the specific model identifier (e.g., "gpt-4", "gemini-pro")
     * @param prompt       the instruction prompt guiding text generation
     * @return the generated natural language text description
     * @throws ResponseStatusException if the LLM API call fails
     */
    public String generateText(String processModel, String providerName, String apiKey, 
                               String model, String prompt) {
        logger.info("Generating text with provider={}, model={}", providerName, model);

        // Transform PNML to BPMN if needed (LLMs work better with BPMN)
        String preparedModel = prepareProcessModel(processModel);

        // Build the LLM request
        LlmRequest request = LlmRequest.builder()
                .apiKey(apiKey)
                .model(model)
                .userPrompt(prompt)
                .content(preparedModel)
                .build();

        try {
            LlmProvider provider = providerFactory.getProvider(providerName);
            LlmResponse response = provider.generateText(request);

            logger.info("Text generation successful: {}", response);
            return response.getContent();

        } catch (LlmException e) {
            logger.error("LLM error: {}", e.toString());
            throw new ResponseStatusException(e.getHttpStatus(), e.getMessage(), e);
        }
    }

    /**
     * Retrieves the list of available models for a given LLM provider.
     *
     * <p>Queries the provider's API to fetch currently available models.
     * For cloud providers (OpenAI, Gemini), this requires a valid API key.
     *
     * @param providerName the LLM provider name: "openai", "gemini", or "lmstudio"
     * @param apiKey       the API key for authentication (may be null for LM Studio)
     * @return list of available model identifiers
     * @throws ResponseStatusException if the API call fails
     */
    public List<String> getAvailableModels(String providerName, String apiKey) {
        logger.info("Fetching models for provider={}", providerName);

        try {
            LlmProvider provider = providerFactory.getProvider(providerName);
            return provider.getAvailableModels(apiKey);

        } catch (LlmException e) {
            logger.error("Failed to fetch models: {}", e.toString());
            throw new ResponseStatusException(e.getHttpStatus(), e.getMessage(), e);
        }
    }

    /**
     * Returns the list of all supported LLM provider names.
     *
     * @return list of provider names (e.g., ["openai", "gemini", "lmstudio"])
     */
    public List<String> getSupportedProviders() {
        return providerFactory.getAvailableProviderNames();
    }

    /**
     * Checks whether a specific provider requires an API key for authentication.
     *
     * @param providerName the provider name to check
     * @return {@code true} if the provider requires an API key,
     *         {@code false} otherwise (e.g., for local providers like LM Studio)
     */
    public boolean providerRequiresApiKey(String providerName) {
        try {
            return providerFactory.getProvider(providerName).requiresApiKey();
        } catch (LlmException e) {
            // Default to requiring key for unknown providers as a safety measure
            return true;
        }
    }

    /**
     * Prepares the process model for optimal LLM processing.
     *
     * <p>LLMs generally produce better results with BPMN format due to its
     * more descriptive element names and clearer structure. This method
     * automatically converts PNML to BPMN when necessary.
     *
     * @param processModel the original process model (PNML or BPMN)
     * @return the prepared model in BPMN format
     */
    private String prepareProcessModel(String processModel) {
        String format = transformerService.checkForBPMNorPNML(processModel);

        if ("PNML".equals(format)) {
            logger.info("Converting PNML to BPMN for better LLM processing");
            return transformerService.transform("pnmltobpmn", processModel);
        }

        return processModel;
    }

    // =========================================================================
    // Legacy API compatibility methods (deprecated, use new methods instead)
    // =========================================================================

    /**
     * Generates text using the legacy DTO format.
     *
     * @param body the process model content
     * @param dto  the legacy API DTO containing provider, key, model, and prompt
     * @return the generated text
     * @deprecated Use {@link #generateText(String, String, String, String, String)} instead.
     *             This method will be removed in a future release.
     */
    @Deprecated(forRemoval = true)
    public String callLLM(String body, de.dhbw.woped.process2text.model.process.OpenAiApiDTO dto) {
        return generateText(body, dto.getProvider(), dto.getApiKey(), dto.getGptModel(), dto.getPrompt());
    }

    /**
     * Retrieves available OpenAI models.
     *
     * @param apiKey the OpenAI API key
     * @return list of available model identifiers
     * @deprecated Use {@link #getAvailableModels(String, String)} with provider "openai" instead.
     *             This method will be removed in a future release.
     */
    @Deprecated(forRemoval = true)
    public List<String> getGptModels(String apiKey) {
        return getAvailableModels("openai", apiKey);
    }

    /**
     * Retrieves available Google Gemini models.
     *
     * @param apiKey the Gemini API key
     * @return list of available model identifiers
     * @deprecated Use {@link #getAvailableModels(String, String)} with provider "gemini" instead.
     *             This method will be removed in a future release.
     */
    @Deprecated(forRemoval = true)
    public List<String> getGeminiModels(String apiKey) {
        return getAvailableModels("gemini", apiKey);
    }

    /**
     * Retrieves available LM Studio models.
     *
     * @return list of available model identifiers
     * @deprecated Use {@link #getAvailableModels(String, String)} with provider "lmstudio" instead.
     *             This method will be removed in a future release.
     */
    @Deprecated(forRemoval = true)
    public List<String> getLmStudioModels() {
        return getAvailableModels("lmstudio", null);
    }
}
