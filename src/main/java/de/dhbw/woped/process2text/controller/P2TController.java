package de.dhbw.woped.process2text.controller;

import de.dhbw.woped.process2text.service.P2TLLMService;
import de.dhbw.woped.process2text.service.P2TService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@Slf4j
public class P2TController {

  Logger logger = LoggerFactory.getLogger(P2TController.class);

  @Autowired private P2TService p2tService;

  @Autowired private P2TLLMService llmService;

  @ApiOperation(value = "Translate a process model into human readable text.")
  @PostMapping(value = "/generateText", consumes = "text/plain", produces = "text/plain")
  protected String generateText(
      @RequestBody String body, @RequestParam(required = false) String apiKey) {
    if (logger.isDebugEnabled()) {
      logger.debug(body.replaceAll("[\n\r\t]", "_"));
    }

    String generatedText = p2tService.generateText(body);

    if (apiKey != null && !apiKey.isEmpty()) {
      return llmService.callLLM(generatedText, apiKey);
    }

    return generatedText;
  }
}
