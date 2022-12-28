package de.dhbw.woped.process2text.service.text.generation;

import de.dhbw.woped.process2text.model.reader.bpmn.BPMNReader;
import de.dhbw.woped.process2text.model.dsynt.DSynTSentence;
import de.dhbw.woped.process2text.model.reader.pnml.PNMLReader;
import de.dhbw.woped.process2text.model.reader.pnml.PetriNet.PetriNet;
import de.dhbw.woped.process2text.model.reader.pnml.PetriNetToProcessConverter;
import de.dhbw.woped.process2text.model.process.ProcessModel;
import de.dhbw.woped.process2text.service.content.determination.labelAnalysis.EnglishLabelDeriver;
import de.dhbw.woped.process2text.service.content.determination.labelAnalysis.EnglishLabelHelper;
import de.dhbw.woped.process2text.service.content.determination.preprocessing.FormatConverter;
import de.dhbw.woped.process2text.service.content.determination.preprocessing.RigidStructurer;
import de.dhbw.woped.process2text.service.sentence.planning.DiscourseMarker;
import de.dhbw.woped.process2text.service.sentence.planning.ReferringExpressionGenerator;
import de.dhbw.woped.process2text.service.sentence.planning.SentenceAggregator;
import de.dhbw.woped.process2text.service.sentence.realization.SurfaceRealizer;
import de.dhbw.woped.process2text.service.text.planning.PlanningHelper;
import de.dhbw.woped.process2text.service.text.planning.TextPlanner;
import de.hpi.bpt.graph.algo.rpst.RPST;
import de.hpi.bpt.process.ControlFlow;
import de.hpi.bpt.process.Node;
import de.hpi.bpt.process.Process;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class TextGenerator {
  ////////////////////////////////////

  Logger logger = LoggerFactory.getLogger(TextGenerator.class);

  public String testGenerator;
  public List<String> roleList;
  private String contextPath = "";

  public TextGenerator() {
    this.contextPath = contextPath;
  }

  public String toText(String input) throws Exception {
    return toText(input, false);
  }

  public String toText(String input, boolean surfaceOnly) throws Exception {
    String imperativeRole = "";
    ByteArrayInputStream is = new ByteArrayInputStream(input.getBytes());
    ByteArrayInputStream helpis = new ByteArrayInputStream(input.getBytes());
    DocumentBuilderFactory helpdbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder helpdb = helpdbf.newDocumentBuilder();
    Document helpdoc = helpdb.parse(helpis);
    helpdoc.getDocumentElement().normalize();
    NodeList isPnml = helpdoc.getElementsByTagName("pnml");
    ProcessModel model = null;
    HashMap<Integer, String> transformedElemsRev = null;
    NodeList isBPMN = helpdoc.getElementsByTagName("bpmn:process");
    if (isPnml.getLength() > 0) {
      PNMLReader pnmlReader = new PNMLReader();
      PetriNet petriNet = pnmlReader.getPetriNetFromPNMLString(is);
      PetriNetToProcessConverter pnConverter = new PetriNetToProcessConverter();
      model = pnConverter.convertToProcess(petriNet);
      transformedElemsRev = pnConverter.transformedElemsRev;
      pnConverter.printConversion();
      logger.info("PNML");
    } else if (isBPMN.getLength() > 0) {
      BPMNReader bpmnReader = new BPMNReader();
      model = bpmnReader.getProcessModelFromBPMNString(is);
      transformedElemsRev = bpmnReader.transformedElemsRev;
      logger.info("BPMN");
    }

    // check number splits/joins

    // HashMap<Integer, String>
    if (model != null && transformedElemsRev != null) {
      for (Integer keys : transformedElemsRev.keySet()) {
        logger.info("" + keys);
        logger.info(transformedElemsRev.get(keys));
      }

      EnglishLabelHelper lHelper = new EnglishLabelHelper();
      EnglishLabelDeriver lDeriver = new EnglishLabelDeriver(lHelper);

      // Annotate model
      model.annotateModel(lDeriver, lHelper);
      // Convert to RPST
      FormatConverter formatConverter = new FormatConverter();
      Process p = formatConverter.transformToRPSTFormat(model);
      RPST<ControlFlow, Node> rpst = new RPST<>(p);

      // Check for Rigids
      boolean containsRigids = PlanningHelper.containsRigid(rpst.getRoot(), rpst);
      // Structure Rigid and convert back
      if (containsRigids) {
        p = formatConverter.transformToRigidFormat(model);
        RigidStructurer rigidStructurer = new RigidStructurer();
        p = rigidStructurer.structureProcess(p);
        model = formatConverter.transformFromRigidFormat(p);
        p = formatConverter.transformToRPSTFormat(model);
        rpst = new RPST<>(p);
      }

      // Convert to Text
      TextPlanner converter =
          new TextPlanner(rpst, model, lDeriver, lHelper, imperativeRole, false, false);
      converter.convertToText(rpst.getRoot(), 0);
      ///////////////////////////////////////////////////////////
      //        String test = converter.testGetRole();
      ArrayList<DSynTSentence> sentencePlan = converter.getSentencePlan();

      // Aggregation
      SentenceAggregator sentenceAggregator = new SentenceAggregator();
      sentencePlan = sentenceAggregator.performRoleAggregation(sentencePlan);

      // Referring Expression
      ReferringExpressionGenerator refExpGenerator = new ReferringExpressionGenerator(lHelper);
      refExpGenerator.insertReferringExpressions(sentencePlan, false);
      sentencePlan = refExpGenerator.insertReferringExpressions(sentencePlan, false);
      roleList = refExpGenerator.generateRoleList(sentencePlan);

      // Discourse Marker
      DiscourseMarker discourseMarker = new DiscourseMarker();
      sentencePlan = discourseMarker.insertSequenceConnectives(sentencePlan);

      // Realization
      SurfaceRealizer surfaceRealizer = new SurfaceRealizer();
      String surfaceText = surfaceRealizer.realizeSentenceMap(sentencePlan, transformedElemsRev);

      // Cleaning
      surfaceText = surfaceRealizer.postProcessText(surfaceText);

      /*if (surfaceOnly) {
          return surfaceText;
      }*/

      return surfaceText;
    } else {
      return "Bitte Datei im BPMN- oder PNML-Format verwenden.";
    }
  }
}
