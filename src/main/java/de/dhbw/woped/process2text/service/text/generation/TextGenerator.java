package de.dhbw.woped.process2text.service.text.generation;

import de.dhbw.woped.process2text.model.dsynt.DSynTSentence;
import de.dhbw.woped.process2text.model.process.ProcessModel;
import de.dhbw.woped.process2text.model.reader.bpmn.BPMNReader;
import de.dhbw.woped.process2text.model.reader.pnml.PNMLReader;
import de.dhbw.woped.process2text.model.reader.pnml.PetriNetToProcessConverter;
import de.dhbw.woped.process2text.model.reader.pnml.petri_net.PetriNet;
import de.dhbw.woped.process2text.service.content.determination.label_analysis.EnglishLabelDeriver;
import de.dhbw.woped.process2text.service.content.determination.label_analysis.EnglishLabelHelper;
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

/**
 * Main orchestrator for algorithmic process-to-text generation.
 *
 * <p>This class implements the core text generation pipeline, converting business
 * process models (PNML or BPMN) into natural language descriptions through a
 * series of NLP transformations.
 *
 * <h2>Processing Pipeline</h2>
 * <ol>
 *   <li><b>Format Detection:</b> Identifies whether input is PNML or BPMN</li>
 *   <li><b>Model Parsing:</b> Converts XML to internal ProcessModel representation</li>
 *   <li><b>Label Analysis:</b> Extracts verbs, objects, and roles from activity labels</li>
 *   <li><b>RPST Conversion:</b> Transforms to Refined Process Structure Tree</li>
 *   <li><b>Rigid Structuring:</b> Handles unstructured process regions</li>
 *   <li><b>Text Planning:</b> Converts process elements to sentence plans</li>
 *   <li><b>Sentence Aggregation:</b> Combines related sentences</li>
 *   <li><b>Referring Expressions:</b> Adds pronouns and references</li>
 *   <li><b>Discourse Markers:</b> Inserts connectives (then, meanwhile, etc.)</li>
 *   <li><b>Surface Realization:</b> Generates final natural language text</li>
 * </ol>
 *
 * <h2>Supported Input Formats</h2>
 * <ul>
 *   <li>PNML (Petri Net Markup Language) - detected by {@code <pnml>} tag</li>
 *   <li>BPMN 2.0 - detected by {@code <bpmn:process>} tag</li>
 * </ul>
 *
 * @author WoPeD Team
 * @see TextPlanner
 * @see SurfaceRealizer
 * @see de.dhbw.woped.process2text.service.P2TService
 */
public class TextGenerator {

  private static final Logger logger = LoggerFactory.getLogger(TextGenerator.class);

  /** For testing purposes - stores generated output */
  public String testGenerator;
  
  /** List of roles/actors extracted from the process */
  public List<String> roleList;
  
  /** Context path (currently unused, kept for compatibility) */
  private String contextPath = "";

  /**
   * Constructs a new TextGenerator instance.
   */
  public TextGenerator() {
    this.contextPath = contextPath;
  }

  /**
   * Converts a process model to natural language text.
   *
   * <p>This is the main entry point for text generation. It orchestrates the
   * entire NLP pipeline from XML input to final text output.
   *
   * @param input the process model in PNML or BPMN XML format
   * @return the generated natural language text description
   * @throws Exception if XML parsing or text generation fails
   */
  public String toText(String input) throws Exception {
    String imperativeRole = "";
    ByteArrayInputStream is = new ByteArrayInputStream(input.getBytes());
    ByteArrayInputStream helpis = new ByteArrayInputStream(input.getBytes());
    DocumentBuilderFactory helpdbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder helpdb = helpdbf.newDocumentBuilder();
    Document helpdoc = helpdb.parse(helpis);
    helpdoc.getDocumentElement().normalize();
    
    // Detect input format by checking for format-specific XML tags
    NodeList isPnml = helpdoc.getElementsByTagName("pnml");
    ProcessModel model = null;
    HashMap<Integer, String> transformedElemsRev = null;
    NodeList isBPMN = helpdoc.getElementsByTagName("bpmn:process");
    
    // Parse PNML format
    if (isPnml.getLength() > 0) {
      PNMLReader pnmlReader = new PNMLReader();
      PetriNet petriNet = pnmlReader.getPetriNetFromPNMLString(is);
      PetriNetToProcessConverter pnConverter = new PetriNetToProcessConverter();
      model = pnConverter.convertToProcess(petriNet);
      transformedElemsRev = pnConverter.transformedElemsRev;
      pnConverter.printConversion();
      logger.info("PNML");
    // Parse BPMN format
    } else if (isBPMN.getLength() > 0) {
      BPMNReader bpmnReader = new BPMNReader();
      model = bpmnReader.getProcessModelFromBPMNString(is);
      transformedElemsRev = bpmnReader.transformedElemsRev;
      logger.info("BPMN");
    }

    // Process the model if successfully parsed
    if (model != null && transformedElemsRev != null) {
      // Log transformed elements for debugging
      for (Integer keys : transformedElemsRev.keySet()) {
        logger.info("Element ID: {}", keys);
        logger.info("Element Name: {}", transformedElemsRev.get(keys));
      }

      // Initialize NLP helpers for English language processing
      EnglishLabelHelper lHelper = new EnglishLabelHelper();
      EnglishLabelDeriver lDeriver = new EnglishLabelDeriver(lHelper);

      // Step 1: Annotate the model with linguistic information
      model.annotateModel(lDeriver, lHelper);
      
      // Step 2: Convert to Refined Process Structure Tree (RPST)
      FormatConverter formatConverter = new FormatConverter();
      Process p = formatConverter.transformToRPSTFormat(model);
      RPST<ControlFlow, Node> rpst = new RPST<>(p);

      // Step 3: Handle rigid (unstructured) regions if present
      boolean containsRigids = PlanningHelper.containsRigid(rpst.getRoot(), rpst);
      if (containsRigids) {
        p = formatConverter.transformToRigidFormat(model);
        RigidStructurer rigidStructurer = new RigidStructurer();
        p = rigidStructurer.structureProcess(p);
        model = formatConverter.transformFromRigidFormat(p);
        p = formatConverter.transformToRPSTFormat(model);
        rpst = new RPST<>(p);
      }

      // Step 4: Convert process structure to sentence plan
      TextPlanner converter =
          new TextPlanner(rpst, model, lDeriver, lHelper, imperativeRole, false, false);
      converter.convertToText(rpst.getRoot(), 0);
      ArrayList<DSynTSentence> sentencePlan = converter.getSentencePlan();

      // Step 5: Aggregate sentences by role
      SentenceAggregator sentenceAggregator = new SentenceAggregator();
      sentencePlan = sentenceAggregator.performRoleAggregation(sentencePlan);

      // Step 6: Insert referring expressions (pronouns, etc.)
      ReferringExpressionGenerator refExpGenerator = new ReferringExpressionGenerator(lHelper);
      refExpGenerator.insertReferringExpressions(sentencePlan, false);
      sentencePlan = refExpGenerator.insertReferringExpressions(sentencePlan, false);
      roleList = refExpGenerator.generateRoleList(sentencePlan);

      // Step 7: Insert discourse markers (then, meanwhile, etc.)
      DiscourseMarker discourseMarker = new DiscourseMarker();
      sentencePlan = discourseMarker.insertSequenceConnectives(sentencePlan);

      // Step 8: Realize surface text from sentence plan
      SurfaceRealizer surfaceRealizer = new SurfaceRealizer();
      String surfaceText = surfaceRealizer.realizeSentenceMap(sentencePlan, transformedElemsRev);

      // Step 9: Post-process and clean the final text
      surfaceText = surfaceRealizer.postProcessText(surfaceText);

      return surfaceText;
    } else {
      // Return error message for unsupported formats
      return "Please use a file in BPMN or PNML format.";
    }
  }
}
