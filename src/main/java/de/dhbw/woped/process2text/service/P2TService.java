package de.dhbw.woped.process2text.service;

import de.dhbw.woped.process2text.textGenerator.TextGenerator;
import org.springframework.stereotype.Service;

@Service
public class P2TService {

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
      output = tg.toText(preparedText, true);

    } catch (Exception e) {
      e.printStackTrace();
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
