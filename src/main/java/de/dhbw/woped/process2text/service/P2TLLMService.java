package de.dhbw.woped.process2text.service;

import de.dhbw.woped.process2text.model.process.OpenAiApiDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

@Service
public class P2TLLMService {

  private static final Logger logger = LoggerFactory.getLogger(P2TLLMService.class);

  public String callLLM(String text, OpenAiApiDTO openAiApiDTO) {
    String apiUrl = "https://api.openai.com/v1/chat/completions";
    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + openAiApiDTO.getApiKey());
    headers.setContentType(MediaType.APPLICATION_JSON);

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
      return restTemplate.postForObject(apiUrl, entity, String.class);
    } catch (HttpClientErrorException e) {
      logger.error("Error calling OpenAI API: {}", e.getResponseBodyAsString());
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "OpenAI API error: " + e.getResponseBodyAsString(), e);
    } catch (RestClientException e) {
      logger.error("Error calling OpenAI API", e);
      throw e; // Re-throw the exception to be caught in the controller
    }
  }
}
