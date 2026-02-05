package de.dhbw.woped.process2text.service.llm.provider;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import de.dhbw.woped.process2text.config.ServiceConfig;
import de.dhbw.woped.process2text.service.llm.LlmException;
import de.dhbw.woped.process2text.service.llm.LlmException.ErrorType;
import de.dhbw.woped.process2text.service.llm.LlmProvider;
import de.dhbw.woped.process2text.service.llm.dto.LlmRequest;
import de.dhbw.woped.process2text.service.llm.dto.LlmResponse;
import java.util.ArrayList;
import java.util.List;
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
 * OpenAI API provider implementation.
 *
 * <p>This class implements the {@link LlmProvider} interface for OpenAI's API,
 * supporting GPT-3.5-turbo, GPT-4, and other chat completion models.
 *
 * <h2>Supported Models</h2>
 * <ul>
 *   <li>gpt-4-turbo</li>
 *   <li>gpt-4</li>
 *   <li>gpt-3.5-turbo</li>
 *   <li>And other models available via the API</li>
 * </ul>
 *
 * <h2>Authentication</h2>
 * <p>Requires a valid OpenAI API key for all operations. API keys can be
 * obtained from the OpenAI dashboard.
 *
 * <h2>Configuration</h2>
 * <p>The API base URL is configured via {@link ServiceConfig#getOpenaiApiUrl()}.
 * Default is {@code https://api.openai.com/v1}.
 *
 * @author WoPeD Team
 * @see LlmProvider
 * @see ServiceConfig
 */
@Component
public class OpenAiProvider implements LlmProvider {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiProvider.class);
    private static final String PROVIDER_NAME = "openai";

    private final ServiceConfig serviceConfig;

    /**
     * Constructs an OpenAiProvider with the given configuration.
     *
     * @param serviceConfig the service configuration containing the OpenAI API URL
     */
    public OpenAiProvider(ServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    /**
     * {@inheritDoc}
     *
     * @return "openai"
     */
    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    /**
     * Generates text using the OpenAI Chat Completions API.
     *
     * <p>Uses the official OpenAI Java SDK for reliable API communication.
     * The request is structured with system and user messages following
     * OpenAI's chat completion format.
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

        String baseUrl = serviceConfig.getOpenaiApiUrl();
        logger.info("Calling OpenAI API at {} with model {}", baseUrl, request.getModel());

        try {
            // Build the OpenAI client with API key and base URL
            OpenAIClient client = OpenAIOkHttpClient.builder()
                    .apiKey(request.getApiKey())
                    .baseUrl(baseUrl)
                    .build();

            // Construct the chat completion request
            ChatCompletionCreateParams createParams = ChatCompletionCreateParams.builder()
                    .model(request.getModel())
                    .maxCompletionTokens(request.getMaxTokens())
                    .temperature(request.getTemperature())
                    .addSystemMessage(request.getSystemPrompt())
                    .addUserMessage(request.getUserPrompt())
                    .addUserMessage(request.getContent())
                    .build();

            // Execute the API call
            ChatCompletion chatCompletion = client.chat().completions().create(createParams);

            // Extract the response content
            String responseContent = chatCompletion.choices().get(0).message().content()
                    .orElseThrow(() -> new LlmException(ErrorType.EMPTY_RESPONSE, PROVIDER_NAME, 
                            "OpenAI returned empty response"));

            logger.info("OpenAI API call successful, response length: {}", responseContent.length());

            return LlmResponse.builder()
                    .content(responseContent)
                    .model(request.getModel())
                    .provider(PROVIDER_NAME)
                    .build();

        } catch (LlmException e) {
            throw e;
        } catch (Exception e) {
            logger.error("OpenAI API call failed: {}", e.getMessage(), e);
            throw new LlmException(ErrorType.UNKNOWN_ERROR, PROVIDER_NAME, 
                    "Error calling OpenAI API: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves the list of available models from the OpenAI API.
     *
     * <p>Calls the {@code /models} endpoint to fetch all models accessible
     * with the provided API key. The returned list includes all model IDs.
     *
     * @param apiKey the OpenAI API key for authentication
     * @return list of model identifiers
     * @throws LlmException with {@link ErrorType#MISSING_API_KEY} if API key is missing
     * @throws LlmException with {@link ErrorType#AUTHENTICATION_FAILED} if API key is invalid
     * @throws LlmException with {@link ErrorType#NETWORK_ERROR} for connectivity issues
     */
    @Override
    public List<String> getAvailableModels(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new LlmException(ErrorType.MISSING_API_KEY, PROVIDER_NAME, 
                    "API key required to list OpenAI models");
        }

        String url = serviceConfig.getOpenaiApiUrl() + "/models";
        logger.info("Fetching available models from OpenAI: {}", url);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            String response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray models = jsonResponse.getJSONArray("data");

            List<String> modelNames = new ArrayList<>();
            for (int i = 0; i < models.length(); i++) {
                JSONObject model = models.getJSONObject(i);
                modelNames.add(model.getString("id"));
            }

            logger.info("Found {} OpenAI models", modelNames.size());
            return modelNames;

        } catch (HttpClientErrorException.Unauthorized e) {
            throw new LlmException(ErrorType.AUTHENTICATION_FAILED, PROVIDER_NAME, 
                    "Invalid OpenAI API key");
        } catch (HttpClientErrorException e) {
            throw new LlmException(ErrorType.UNKNOWN_ERROR, PROVIDER_NAME, 
                    "OpenAI API error: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            logger.error("Failed to fetch OpenAI models: {}", e.getMessage(), e);
            throw new LlmException(ErrorType.NETWORK_ERROR, PROVIDER_NAME, 
                    "Failed to fetch models from OpenAI", e);
        }
    }

    /**
     * Validates that the request contains all required fields for OpenAI.
     *
     * @param request the request to validate
     * @throws LlmException if validation fails
     */
    private void validateRequest(LlmRequest request) {
        if (request.getApiKey() == null || request.getApiKey().trim().isEmpty()) {
            throw new LlmException(ErrorType.MISSING_API_KEY, PROVIDER_NAME, 
                    "OpenAI API key is required");
        }
        if (request.getModel() == null || request.getModel().trim().isEmpty()) {
            throw new LlmException(ErrorType.MISSING_MODEL, PROVIDER_NAME, 
                    "Model selection is required for OpenAI");
        }
    }
}
