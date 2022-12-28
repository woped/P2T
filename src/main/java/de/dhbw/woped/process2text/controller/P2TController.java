package de.dhbw.woped.process2text.controller;

import de.dhbw.woped.process2text.service.P2TService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class P2TController {

  public static final int MAX_INPUT_LENGTH = 15000; // Reject any Request larger than this
  Logger logger = LoggerFactory.getLogger(P2TController.class);

  @Autowired P2TService p2tService;

  @ApiOperation(value = "Translate a process model into human readable text.")
  @RequestMapping(
      value = "/generateText",
      method = RequestMethod.POST,
      consumes = "text/plain",
      produces = "text/plain")
  protected String doPost(@RequestBody String body) {
    logger.debug(body.replaceAll("[\n\r\t]", "_"));
    return p2tService.generateText(body);
  }
}
