package de.dhbw.woped.process2text.service;

import de.dhbw.woped.process2text.service.text.generation.TextGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class P2TService {

  Logger logger = LoggerFactory.getLogger(P2TService.class);

  /**
   * Generate text from a process model
   *
   * @param text
   * @return
   */
  public String generateText(String text) {
    String preparedText = prepareText(text);
    String output = "";
    TextGenerator tg = new TextGenerator();

    try {
      output = tg.toText(preparedText);

    } catch (Exception e) {
      logger.error(e.getLocalizedMessage());
    }
    return output;
  }

  /**
   * In case the pnml in send via an html form, it starts with the name of the input form. For
   * further processing, this part must be removed.
   *
   * @param pnml the pnml
   * @return a valid xml
   */
  private String prepareText(String pnml) {
    pnml = pnml.trim();
    return pnml.startsWith("=") ? pnml.substring(1) : pnml;
  }
}
