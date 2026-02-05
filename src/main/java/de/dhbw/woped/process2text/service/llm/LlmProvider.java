package de.dhbw.woped.process2text.service.llm;

import de.dhbw.woped.process2text.service.llm.dto.LlmRequest;
import de.dhbw.woped.process2text.service.llm.dto.LlmResponse;
import java.util.List;

/**
 * Strategy interface for Large Language Model (LLM) providers.
 *
 * <p>This interface defines the contract for LLM provider implementations,
 * allowing the application to work with multiple LLM services in a unified way.
 * It follows the Strategy design pattern, enabling runtime selection of the
 * appropriate provider based on user configuration.
 *
 * <h2>Implementation Requirements</h2>
 * <ul>
 *   <li>Each implementation must be annotated with {@code @Component} or {@code @Service}</li>
 *   <li>Provider names must be unique and lowercase</li>
 *   <li>Implementations should throw {@link LlmException} for API errors</li>
 *   <li>Response parsing should be robust against API changes</li>
 * </ul>
 *
 * <h2>Available Implementations</h2>
 * <ul>
 *   <li>{@code OpenAiProvider} - OpenAI GPT models</li>
 *   <li>{@code GeminiProvider} - Google Gemini models</li>
 *   <li>{@code LmStudioProvider} - Local models via LM Studio</li>
 * </ul>
 *
 * @author WoPeD Team
 * @see LlmProviderFactory
 * @see de.dhbw.woped.process2text.service.llm.provider.OpenAiProvider
 * @see de.dhbw.woped.process2text.service.llm.provider.GeminiProvider
 * @see de.dhbw.woped.process2text.service.llm.provider.LmStudioProvider
 */
public interface LlmProvider {

    /**
     * Returns the unique identifier for this provider.
     *
     * <p>The name should be lowercase and match what users specify when
     * selecting a provider (e.g., "openai", "gemini", "lmstudio").
     *
     * @return the provider name, never null
     */
    String getProviderName();

    /**
     * Generates text using the LLM API.
     *
     * <p>This is the core method for text generation. Implementations should:
     * <ul>
     *   <li>Construct appropriate API requests for their specific provider</li>
     *   <li>Handle rate limiting and retries if applicable</li>
     *   <li>Parse the response and extract the generated text</li>
     *   <li>Throw appropriate {@link LlmException} subtypes for errors</li>
     * </ul>
     *
     * @param request the LLM request containing API key, model, prompt, and content
     * @return the generated response containing the text and metadata
     * @throws LlmException if the API call fails (authentication, rate limit, network, etc.)
     */
    LlmResponse generateText(LlmRequest request);

    /**
     * Retrieves the list of available models from the provider.
     *
     * <p>This method queries the provider's API to get the currently available
     * models. The returned list may vary based on the user's API access level.
     *
     * @param apiKey the API key for authentication (may be null for local providers)
     * @return list of available model identifiers (e.g., ["gpt-4", "gpt-3.5-turbo"])
     * @throws LlmException if the API call fails
     */
    List<String> getAvailableModels(String apiKey);

    /**
     * Indicates whether this provider requires an API key for authentication.
     *
     * <p>Cloud providers (OpenAI, Gemini) typically require API keys, while
     * local providers (LM Studio) do not.
     *
     * @return {@code true} if an API key is required, {@code false} otherwise
     */
    default boolean requiresApiKey() {
        return true;
    }
}
