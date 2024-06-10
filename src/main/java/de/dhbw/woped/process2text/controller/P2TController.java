package de.dhbw.woped.process2text.controller;

import de.dhbw.woped.process2text.model.process.EnumGptModel;
import de.dhbw.woped.process2text.model.process.OpenAiApiDTO;
import de.dhbw.woped.process2text.service.P2TLLMService;
import de.dhbw.woped.process2text.service.P2TService;
import io.swagger.annotations.ApiOperation;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@Slf4j
public class P2TController {

  Logger logger = LoggerFactory.getLogger(P2TController.class);

  @Autowired private P2TService p2tService;

  @Autowired private P2TLLMService llmService;

  @ApiOperation(value = "Translate a process model into human readable text.")
  @PostMapping(value = "/generateText", consumes = "text/plain", produces = "text/plain")
  protected String generateText(@RequestBody String body) {
    if (logger.isDebugEnabled()) {
      logger.debug("Received body: " + body.replaceAll("[\n\r\t]", "_"));
    }
    String response = p2tService.generateText(body);
    logger.debug("Response: " + response);
    return response;
  }

  @ApiOperation(
      value =
          "Translate a process model into human readable text using OpenAIs Large Language Model"
              + " GPT-4 Turbo.")
  @PostMapping(value = "/generateTextLLM", consumes = "text/plain", produces = "text/plain")
  protected String generateTextLLM(
      @RequestBody String body,
      @RequestParam(required = true) String apiKey,
      @RequestParam(required = true) String prompt,
      @RequestParam(required = true) String gptModel) {
    logger.debug(
        "Received request with apiKey: {}, prompt: {}, gptModel: {}, body: {}",
        apiKey,
        prompt,
        gptModel,
        body.replaceAll("[\n\r\t]", "_"));
    OpenAiApiDTO openAiApiDTO =
        new OpenAiApiDTO(apiKey, EnumGptModel.getEnumGptModel(gptModel), prompt);
    String response = llmService.callLLM(body, openAiApiDTO);
    logger.debug("LLM Response: " + response);
    return response;
  }

  @GetMapping("/gptModels")
  public List<String> getEnumGptModels() {

    return Arrays.asList(EnumGptModel.values()).stream()
        .map(e -> e.getModel())
        .collect(Collectors.toList());
  }
}
