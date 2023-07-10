package de.dhbw.woped.process2text.service;

import de.dhbw.woped.process2text.exception.BpmNlpHttpStatus;
import de.dhbw.woped.process2text.exception.RPSTConvertionException;
import de.dhbw.woped.process2text.exception.StructureProcessModelException;
import de.dhbw.woped.process2text.service.text.generation.TextGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Service
public class P2TService {

  Logger logger = LoggerFactory.getLogger(P2TService.class);

  /**
   * Generate text from a process model
   *
   * @param text
   * @return
   */
  public ResponseEntity<String> generateText(String text) {
    String preparedText = prepareText(text);
    String output = "";
    TextGenerator tg = new TextGenerator();

    try {
      output = tg.toText(preparedText);
    }
    catch (RPSTConvertionException rpstConvertionException){
      logger.error(rpstConvertionException.getLocalizedMessage());
      return ResponseEntity.status(BpmNlpHttpStatus.RPST_FAILURE).body(rpstConvertionException.getLocalizedMessage());
    }
    catch (StructureProcessModelException structureProcessModelException){
      logger.error(structureProcessModelException.getLocalizedMessage());
      return ResponseEntity.status(BpmNlpHttpStatus.STRUCTURE_FAILURE).body(structureProcessModelException.getLocalizedMessage());
    }
    catch (Exception e) {
      logger.error(e.getLocalizedMessage());
      return ResponseEntity.status(BpmNlpHttpStatus.CONVERTION_ERROR).body("Error while generating the text out of the process");
    }
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(output);
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
