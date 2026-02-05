package de.dhbw.woped.process2text.service;

import de.dhbw.woped.process2text.service.text.generation.TextGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for algorithmic process-to-text generation.
 *
 * <p>This service converts business process models (PNML or BPMN) into
 * natural language text descriptions using rule-based NLP techniques.
 * Unlike the LLM-based approach, this method does not require external
 * API calls and produces deterministic output.
 *
 * <h2>How It Works</h2>
 * <ol>
 *   <li>Receives process model XML (PNML or BPMN format)</li>
 *   <li>Preprocesses the input to handle form-encoded data</li>
 *   <li>Delegates to {@link TextGenerator} for the actual text generation</li>
 *   <li>Returns the generated natural language description</li>
 * </ol>
 *
 * <h2>Supported Formats</h2>
 * <ul>
 *   <li>PNML (Petri Net Markup Language)</li>
 *   <li>BPMN 2.0 (Business Process Model and Notation)</li>
 * </ul>
 *
 * @author WoPeD Team
 * @see TextGenerator
 * @see de.dhbw.woped.process2text.controller.P2TController#generateText(String)
 */
@Service
public class P2TService {

  private static final Logger logger = LoggerFactory.getLogger(P2TService.class);

  /**
   * Generates a natural language text description from a process model.
   *
   * <p>This method uses the algorithmic text generation approach, which
   * applies NLP techniques to convert process model structures into
   * readable text without requiring LLM API calls.
   *
   * @param text the process model XML content (PNML or BPMN format)
   * @return the generated natural language text description,
   *         or an empty string if generation fails
   */
  public String generateText(String text) {
    String preparedText = prepareText(text);
    String output = "";
    TextGenerator tg = new TextGenerator();

    try {
      output = tg.toText(preparedText);

    } catch (Exception e) {
      logger.error("Text generation failed: {}", e.getLocalizedMessage());
    }
    return output;
  }

  /**
   * Preprocesses the input text to handle HTML form-encoded data.
   *
   * <p>When process model XML is submitted via an HTML form, it may be
   * prefixed with the form field name followed by '='. This method
   * removes such prefixes to ensure valid XML for processing.
   *
   * <p>For example: {@code "pnml=<?xml...>"} becomes {@code "<?xml...>"}
   *
   * @param pnml the raw input text, potentially form-encoded
   * @return clean XML content ready for processing
   */
  private String prepareText(String pnml) {
    pnml = pnml.trim();
    return pnml.startsWith("=") ? pnml.substring(1) : pnml;
  }
}
