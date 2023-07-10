package de.dhbw.woped.process2text.controller;

import de.dhbw.woped.process2text.service.P2TService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class P2TController {

  Logger logger = LoggerFactory.getLogger(P2TController.class);

  @Autowired P2TService p2tService;

  @ApiOperation(value = "Translate a process model into human readable text.")
  @PostMapping(value = "/generateText", consumes = "text/plain", produces = "text/plain")
  protected ResponseEntity<String> generateText(@RequestBody String body) {
    if (logger
        .isDebugEnabled()) { // required so that body.replaceAll is only invoked in case the body is
      // logged
      logger.debug(body.replaceAll("[\n\r\t]", "_"));
    }
    return p2tService.generateText(body);
  }
}
