package de.dhbw.woped.process2text.service;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class P2TLLMService {

  public String callLLM(String text, String apiKey) {
    String apiUrl = "https://api.openai.com/v1/engines/davinci-codex/completions";
    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + apiKey);
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("prompt", text);
    requestBody.put("max_tokens", 150);

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

    return restTemplate.postForObject(apiUrl, entity, String.class);
  }
  public String callTransformer(String file){

  }

  public String checkForBPMNorPNML(String file){
    if()
  }
}
