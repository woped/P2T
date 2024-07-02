package de.dhbw.woped.process2text;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.dhbw.woped.process2text.service.TransformerService;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

class TransformerServiceTest {

  private final TransformerService transformerService = new TransformerService();

  @Test
  void testCheckForBPMNorPNML_BPMN()
      throws ParserConfigurationException, IOException, SAXException {
    String bpmnXml = "<definitions></definitions>";
    String result = transformerService.checkForBPMNorPNML(bpmnXml);
    assertEquals("BPMN", result);
  }

  @Test
  void testCheckForBPMNorPNML_PNML()
      throws ParserConfigurationException, IOException, SAXException {
    String pnmlXml = "<pnml></pnml>";
    String result = transformerService.checkForBPMNorPNML(pnmlXml);
    assertEquals("PNML", result);
  }

  @Test
  void testCheckForBPMNorPNML_Unknown()
      throws ParserConfigurationException, IOException, SAXException {
    String unknownXml = "<unknown></unknown>";
    String result = transformerService.checkForBPMNorPNML(unknownXml);
    assertEquals("Unknown", result);
  }
}
