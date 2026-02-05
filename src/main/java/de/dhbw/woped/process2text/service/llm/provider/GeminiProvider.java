package de.dhbw.woped.process2text.service.llm.provider;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import de.dhbw.woped.process2text.config.ServiceConfig;
import de.dhbw.woped.process2text.service.llm.LlmException;
import de.dhbw.woped.process2text.service.llm.LlmException.ErrorType;
import de.dhbw.woped.process2text.service.llm.LlmProvider;
import de.dhbw.woped.process2text.service.llm.dto.LlmRequest;
import de.dhbw.woped.process2text.service.llm.dto.LlmResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Google Gemini API provider implementation.
 *
 * <p>This class implements the {@link LlmProvider} interface for Google's
 * Gemini API, supporting Gemini 1.5 Pro, Gemini 1.0 Pro, and other models.
 *
 * <h2>Supported Models</h2>
 * <ul>
 *   <li>gemini-1.5-pro</li>
 *   <li>gemini-1.5-flash</li>
 *   <li>gemini-1.0-pro</li>
 *   <li>And other models available via the API</li>
 * </ul>
 *
 * <h2>Authentication</h2>
 * <p>Requires a valid Google AI Studio API key for all operations.
 * API keys can be obtained from the Google AI Studio dashboard.
 *
 * <h2>Configuration</h2>
 * <p>The API base URL is configured via {@link ServiceConfig#getGeminiApiUrl()}.
 * Default is {@code https://generativelanguage.googleapis.com}.
 *
 * @author WoPeD Team
 * @see LlmProvider
 * @see ServiceConfig
 */
@Component
public class GeminiProvider implements LlmProvider {

    private static final Logger logger = LoggerFactory.getLogger(GeminiProvider.class);
    private static final String PROVIDER_NAME = "gemini";

    private final ServiceConfig serviceConfig;

    /**
     * Constructs a GeminiProvider with the given configuration.
     *
     * @param serviceConfig the service configuration containing the Gemini API URL
     */
    public GeminiProvider(ServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    /**
     * {@inheritDoc}
     *
     * @return "gemini"
     */
    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    /**
     * Generates text using the Google Gemini API.
     *
     * <p>Uses the official Google AI Java SDK for API communication.
     * The request is structured as a multi-turn conversation with
     * system context (as model role) and user message.
     *
     * @param request the LLM request containing API key, model, prompts, and parameters
     * @return the generated response with content and metadata
     * @throws LlmException with {@link ErrorType#MISSING_API_KEY} if API key is missing
     * @throws LlmException with {@link ErrorType#MISSING_MODEL} if model is not specified
     * @throws LlmException with {@link ErrorType#EMPTY_RESPONSE} if API returns no content
     * @throws LlmException with {@link ErrorType#UNKNOWN_ERROR} for other API errors
     */
    @Override
    public LlmResponse generateText(LlmRequest request) {
        validateRequest(request);

        String baseUrl = serviceConfig.getGeminiApiUrl();
        logger.info("Calling Gemini API at {} with model {}", baseUrl, request.getModel());

        try {
            // Configure the base URL for the Gemini client
            Client.setDefaultBaseUrls(Optional.of(baseUrl), Optional.empty());

            // Build the Gemini client with API key
            Client client = Client.builder()
                    .apiKey(request.getApiKey())
                    .build();

            // Configure generation parameters
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .temperature((float) request.getTemperature())
                    .maxOutputTokens(request.getMaxTokens())
                    .build();

            // Build the conversation history
            List<Content> chatMessages = new ArrayList<>();
            // System prompt as model context
            chatMessages.add(Content.builder()
                    .role("model")
                    .parts(List.of(Part.fromText(request.getSystemPrompt())))
                    .build());
            // User message with combined prompt and content
            chatMessages.add(Content.builder()
                    .role("user")
                    .parts(List.of(Part.fromText(request.getCombinedUserMessage())))
                    .build());

            // Execute the API call
            GenerateContentResponse response = client.models.generateContent(
                    request.getModel(), chatMessages, config);

            // Validate and extract response
            if (response == null || response.text() == null || response.text().isEmpty()) {
                throw new LlmException(ErrorType.EMPTY_RESPONSE, PROVIDER_NAME,
                        "Gemini returned empty response for model: " + request.getModel());
            }

            logger.info("Gemini API call successful, response length: {}", response.text().length());

            return LlmResponse.builder()
                    .content(response.text())
                    .model(request.getModel())
                    .provider(PROVIDER_NAME)
                    .build();

        } catch (LlmException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Gemini API call failed: {}", e.getMessage(), e);
            throw new LlmException(ErrorType.UNKNOWN_ERROR, PROVIDER_NAME,
                    "Error calling Gemini API: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves the list of available models from the Gemini API.
     *
     * <p>Calls the {@code /v1beta/models} endpoint to fetch all models
     * accessible with the provided API key.
     *
     * @param apiKey the Gemini API key for authentication
     * @return list of model identifiers
     * @throws LlmException with {@link ErrorType#MISSING_API_KEY} if API key is missing
     * @throws LlmException with {@link ErrorType#AUTHENTICATION_FAILED} if API key is invalid
     * @throws LlmException with {@link ErrorType#NETWORK_ERROR} for connectivity issues
     */
    @Override
    public List<String> getAvailableModels(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new LlmException(ErrorType.MISSING_API_KEY, PROVIDER_NAME,
                    "API key required to list Gemini models");
        }

        String url = serviceConfig.getGeminiApiUrl() + "/v1beta/models";
        logger.info("Fetching available models from Gemini: {}", url);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Goog-Api-Key", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            String response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray models = jsonResponse.getJSONArray("models");

            List<String> modelNames = new ArrayList<>();
            for (int i = 0; i < models.length(); i++) {
                JSONObject model = models.getJSONObject(i);
                modelNames.add(model.getString("name"));
            }

            logger.info("Found {} Gemini models", modelNames.size());
            return modelNames;

        } catch (HttpClientErrorException.Unauthorized e) {
            throw new LlmException(ErrorType.AUTHENTICATION_FAILED, PROVIDER_NAME,
                    "Invalid Gemini API key");
        } catch (HttpClientErrorException e) {
            throw new LlmException(ErrorType.UNKNOWN_ERROR, PROVIDER_NAME,
                    "Gemini API error: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            logger.error("Failed to fetch Gemini models: {}", e.getMessage(), e);
            throw new LlmException(ErrorType.NETWORK_ERROR, PROVIDER_NAME,
                    "Failed to fetch models from Gemini", e);
        }
    }

    /**
     * Validates that the request contains all required fields for Gemini.
     *
     * @param request the request to validate
     * @throws LlmException if validation fails
     */
    private void validateRequest(LlmRequest request) {
        if (request.getApiKey() == null || request.getApiKey().trim().isEmpty()) {
            throw new LlmException(ErrorType.MISSING_API_KEY, PROVIDER_NAME,
                    "Gemini API key is required");
        }
        if (request.getModel() == null || request.getModel().trim().isEmpty()) {
            throw new LlmException(ErrorType.MISSING_MODEL, PROVIDER_NAME,
                    "Model selection is required for Gemini");
        }
    }
}
