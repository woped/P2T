package de.dhbw.woped.process2text.service;

import de.dhbw.woped.process2text.controller.P2TController;
import de.dhbw.woped.process2text.model.process.OpenAiApiDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service class to handle interaction with the OpenAI API. This service sends text to the API and
 * retrieves the generated response.
 */
@Service
public class P2TLLMService {

  private static final Logger logger = LoggerFactory.getLogger(P2TController.class);

  /**
   * Calls the OpenAI API with the provided text and API details, and extracts the response content.
   *
   * @param text The text to be sent to the OpenAI API.
   * @param openAiApiDTO Contains the API key, GPT model, and prompt.
   * @return The content of the response from the OpenAI API.
   */
  public String callLLM(String text, OpenAiApiDTO openAiApiDTO) {
    String apiUrl = "https://api.openai.com/v1/chat/completions";
    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + openAiApiDTO.getApiKey());
    headers.setContentType(MediaType.APPLICATION_JSON);

    // Create the request body with the specified model, messages, max tokens, and
    // temperature
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("model", openAiApiDTO.getGptModel().getModel());
    requestBody.put(
        "messages",
        List.of(
            Map.of("role", "system", "content", "You are a helpful assistant."),
            Map.of("role", "user", "content", openAiApiDTO.getPrompt())));
    requestBody.put("max_tokens", 4096);
    requestBody.put("temperature", 0.7);

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

    try {
      // Send the request to the OpenAI API and get the response as a string
      String response = restTemplate.postForObject(apiUrl, entity, String.class);
      // Parse the response to extract the content
      return extractContentFromResponse(response);
    } catch (HttpClientErrorException e) {
      logger.error("Error calling OpenAI API: {}", e.getResponseBodyAsString());
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "OpenAI API error: " + e.getResponseBodyAsString(), e);
    } catch (RestClientException e) {
      logger.error("Error calling OpenAI API", e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Error calling OpenAI API", e);
    }
  }

  /**
   * Parses the response from the OpenAI API to extract the content.
   *
   * @param response The raw JSON response from the OpenAI API.
   * @return The extracted content from the response.
   */
  private String extractContentFromResponse(String response) {
    try {
      // Assuming the response is a JSON string, parse it
      JSONObject jsonResponse = new JSONObject(response);
      JSONArray choices = jsonResponse.getJSONArray("choices");
      if (choices.length() > 0) {
        // Get the first choice and extract the message content
        JSONObject firstChoice = choices.getJSONObject(0);
        JSONObject message = firstChoice.getJSONObject("message");
        return message.getString("content");
      } else {
        throw new ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR, "No choices found in the response");
      }
    } catch (JSONException e) {
      logger.error("Error parsing OpenAI API response", e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Error parsing OpenAI API response", e);
    }
  }
}
