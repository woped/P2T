package de.dhbw.woped.process2text.service.llm.provider;

import de.dhbw.woped.process2text.config.ServiceConfig;
import de.dhbw.woped.process2text.service.llm.LlmException;
import de.dhbw.woped.process2text.service.llm.LlmException.ErrorType;
import de.dhbw.woped.process2text.service.llm.LlmProvider;
import de.dhbw.woped.process2text.service.llm.dto.LlmRequest;
import de.dhbw.woped.process2text.service.llm.dto.LlmResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * LM Studio API provider implementation for local LLM inference.
 *
 * <p>This class implements the {@link LlmProvider} interface for LM Studio,
 * a desktop application that provides an OpenAI-compatible API for running
 * local Large Language Models. Unlike cloud providers, LM Studio does not
 * require an API key.
 *
 * <h2>Supported Models</h2>
 * <p>LM Studio supports any model loaded in the application, including:
 * <ul>
 *   <li>Llama 2 and Llama 3 variants</li>
 *   <li>Mistral and Mixtral models</li>
 *   <li>Phi models</li>
 *   <li>Any GGUF-compatible model</li>
 * </ul>
 *
 * <h2>Authentication</h2>
 * <p>No API key is required. LM Studio runs locally on the user's machine.
 *
 * <h2>Prerequisites</h2>
 * <ul>
 *   <li>LM Studio must be installed and running</li>
 *   <li>A model must be loaded in LM Studio</li>
 *   <li>The local server must be started in LM Studio</li>
 * </ul>
 *
 * <h2>Configuration</h2>
 * <p>The API URL is configured via {@link ServiceConfig#getLmstudioApiUrl()}.
 * Default is {@code http://localhost:1234}.
 *
 * @author WoPeD Team
 * @see LlmProvider
 * @see ServiceConfig
 */
@Component
public class LmStudioProvider implements LlmProvider {

    private static final Logger logger = LoggerFactory.getLogger(LmStudioProvider.class);
    private static final String PROVIDER_NAME = "lmstudio";

    private final ServiceConfig serviceConfig;
    private final RestTemplate restTemplate;

    /**
     * Constructs an LmStudioProvider with the given configuration.
     *
     * @param serviceConfig the service configuration containing the LM Studio API URL
     */
    public LmStudioProvider(ServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
        this.restTemplate = new RestTemplate();
    }

    /**
     * {@inheritDoc}
     *
     * @return "lmstudio"
     */
    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    /**
     * Indicates that LM Studio does not require an API key.
     *
     * @return {@code false} always, as LM Studio runs locally
     */
    @Override
    public boolean requiresApiKey() {
        return false;
    }

    /**
     * Generates text using the local LM Studio server.
     *
     * <p>Uses the OpenAI-compatible chat completions endpoint provided by
     * LM Studio. The request is formatted according to the OpenAI API spec.
     *
     * <p>Special handling is applied for Mistral models, which require
     * "assistant" instead of "system" for the initial role.
     *
     * @param request the LLM request containing model, prompts, and parameters
     * @return the generated response with content and metadata
     * @throws LlmException with {@link ErrorType#MISSING_MODEL} if model is not specified
     * @throws LlmException with {@link ErrorType#PROVIDER_UNAVAILABLE} if server is not running
     * @throws LlmException with {@link ErrorType#EMPTY_RESPONSE} if API returns no content
     * @throws LlmException with {@link ErrorType#PARSE_ERROR} if response cannot be parsed
     */
    @Override
    public LlmResponse generateText(LlmRequest request) {
        validateRequest(request);

        String apiUrl = buildChatCompletionsUrl();
        logger.info("Calling LM Studio API at {} with model {}", apiUrl, request.getModel());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = buildRequestBody(request);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            String response = restTemplate.postForObject(apiUrl, entity, String.class);
            String content = extractContent(response);

            logger.info("LM Studio API call successful, response length: {}", content.length());

            return LlmResponse.builder()
                    .content(content)
                    .model(request.getModel())
                    .provider(PROVIDER_NAME)
                    .build();

        } catch (ResourceAccessException e) {
            logger.error("Cannot connect to LM Studio: {}", e.getMessage());
            throw new LlmException(ErrorType.PROVIDER_UNAVAILABLE, PROVIDER_NAME,
                    "Cannot connect to LM Studio. Is the server running?", e);
        } catch (HttpClientErrorException e) {
            logger.error("LM Studio API error: {}", e.getResponseBodyAsString());
            throw new LlmException(ErrorType.UNKNOWN_ERROR, PROVIDER_NAME,
                    "LM Studio API error: " + e.getResponseBodyAsString(), e);
        } catch (LlmException e) {
            throw e;
        } catch (Exception e) {
            logger.error("LM Studio API call failed: {}", e.getMessage(), e);
            throw new LlmException(ErrorType.UNKNOWN_ERROR, PROVIDER_NAME,
                    "Error calling LM Studio API: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves the list of models currently loaded in LM Studio.
     *
     * <p>Attempts to fetch models from multiple endpoints for compatibility
     * with different LM Studio versions:
     * <ol>
     *   <li>{@code /v1/models} - Standard OpenAI-compatible endpoint</li>
     *   <li>{@code /api/v0/models} - Alternative endpoint for older versions</li>
     * </ol>
     *
     * @param apiKey ignored for LM Studio (no authentication required)
     * @return list of available model identifiers, or a placeholder message if none found
     */
    @Override
    public List<String> getAvailableModels(String apiKey) {
        // apiKey is ignored for LM Studio
        String baseUrl = getBaseUrl();
        List<String> models = new ArrayList<>();

        // Try standard OpenAI-compatible endpoint first
        try {
            models = fetchModelsFromEndpoint(baseUrl + "/v1/models");
            if (!models.isEmpty()) {
                return models;
            }
        } catch (Exception e) {
            logger.debug("Standard endpoint failed, trying alternative: {}", e.getMessage());
        }

        // Try alternative v0 endpoint
        try {
            models = fetchModelsFromEndpoint(baseUrl + "/api/v0/models");
            if (!models.isEmpty()) {
                return models;
            }
        } catch (Exception e) {
            logger.warn("Both model endpoints failed: {}", e.getMessage());
        }

        // Return placeholder if no models found
        models.add("No models found - check LM Studio server");
        return models;
    }

    /**
     * Fetches model list from a specific endpoint.
     *
     * @param url the full URL of the models endpoint
     * @return list of model IDs
     */
    private List<String> fetchModelsFromEndpoint(String url) {
        logger.info("Fetching models from: {}", url);

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
        JSONObject jsonResponse = new JSONObject(response);
        JSONArray modelsArray = jsonResponse.getJSONArray("data");

        List<String> models = new ArrayList<>();
        for (int i = 0; i < modelsArray.length(); i++) {
            JSONObject model = modelsArray.getJSONObject(i);
            models.add(model.getString("id"));
        }

        logger.info("Found {} LM Studio models", models.size());
        return models;
    }

    /**
     * Builds the request body for the chat completions API.
     *
     * <p>Handles model-specific quirks, such as using "assistant" role
     * instead of "system" for Mistral models.
     *
     * @param request the LLM request
     * @return the request body as a map
     */
    private Map<String, Object> buildRequestBody(LlmRequest request) {
        List<Map<String, String>> messages = new ArrayList<>();

        // Determine first role based on model (Mistral uses "assistant", others use "system")
        String firstRole = request.getModel().toLowerCase().contains("mistral") ? "assistant" : "system";

        messages.add(Map.of("role", firstRole, "content", request.getSystemPrompt()));
        messages.add(Map.of("role", "user", "content", request.getCombinedUserMessage()));

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", request.getModel());
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", request.getMaxTokens());
        requestBody.put("temperature", request.getTemperature());
        requestBody.put("stream", false);

        return requestBody;
    }

    /**
     * Extracts the generated content from the API response.
     *
     * <p>Handles multiple response formats for compatibility with
     * different LM Studio versions:
     * <ul>
     *   <li>{@code choices[0].message.content} - Standard OpenAI format</li>
     *   <li>{@code choices[0].text} - Alternative format</li>
     *   <li>{@code choices[0].content} - Alternative format</li>
     *   <li>{@code content} - Root-level format</li>
     * </ul>
     *
     * @param response the raw JSON response string
     * @return the extracted text content
     * @throws LlmException if content cannot be extracted
     */
    private String extractContent(String response) {
        if (response == null || response.isEmpty()) {
            throw new LlmException(ErrorType.EMPTY_RESPONSE, PROVIDER_NAME,
                    "LM Studio returned empty response");
        }

        try {
            JSONObject jsonResponse = new JSONObject(response);

            // Standard OpenAI format: choices[0].message.content
            if (jsonResponse.has("choices")) {
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices.length() > 0) {
                    JSONObject firstChoice = choices.getJSONObject(0);

                    if (firstChoice.has("message")) {
                        JSONObject message = firstChoice.getJSONObject("message");
                        if (message.has("content")) {
                            return message.getString("content");
                        }
                    }

                    // Alternative formats
                    if (firstChoice.has("text")) {
                        return firstChoice.getString("text");
                    }
                    if (firstChoice.has("content")) {
                        return firstChoice.getString("content");
                    }
                }
            }

            // Root-level content field
            if (jsonResponse.has("content")) {
                return jsonResponse.getString("content");
            }

            throw new LlmException(ErrorType.PARSE_ERROR, PROVIDER_NAME,
                    "Cannot extract content from LM Studio response");

        } catch (LlmException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to parse LM Studio response: {}", response, e);
            throw new LlmException(ErrorType.PARSE_ERROR, PROVIDER_NAME,
                    "Failed to parse LM Studio response", e);
        }
    }

    /**
     * Builds the full URL for the chat completions endpoint.
     *
     * @return the chat completions URL
     */
    private String buildChatCompletionsUrl() {
        String baseUrl = getBaseUrl();
        return baseUrl + "/v1/chat/completions";
    }

    /**
     * Gets the base URL, stripping any endpoint suffixes.
     *
     * @return the base URL without endpoint path
     */
    private String getBaseUrl() {
        String url = serviceConfig.getLmstudioApiUrl();
        // Remove endpoint suffix if present
        if (url.endsWith("/v1/chat/completions")) {
            url = url.replace("/v1/chat/completions", "");
        }
        return url;
    }

    /**
     * Validates that the request contains all required fields.
     *
     * @param request the request to validate
     * @throws LlmException if validation fails
     */
    private void validateRequest(LlmRequest request) {
        if (request.getModel() == null || request.getModel().trim().isEmpty()) {
            throw new LlmException(ErrorType.MISSING_MODEL, PROVIDER_NAME,
                    "Model selection is required for LM Studio");
        }
    }
}
