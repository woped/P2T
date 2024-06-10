package de.dhbw.woped.process2text.service;

import de.dhbw.woped.process2text.model.process.OpenAiApiDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class P2TLLMService {

  public String callLLM(String text, OpenAiApiDTO openAiApiDTO) {
    String apiUrl = "https://api.openai.com/v1/chat/completions";
    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + openAiApiDTO.getApiKey());
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("model", openAiApiDTO.getGptModel());
    requestBody.put(
        "messages",
        List.of(
            Map.of("role", "system", "content", "You are a helpful assistant."),
            Map.of("role", "user", "content", openAiApiDTO.getPrompt())));
    requestBody.put("max_tokens", 4096);
    requestBody.put("temperature", 0.7);

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

    return restTemplate.postForObject(apiUrl, entity, String.class);
  }
}
