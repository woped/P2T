package de.dhbw.woped.process2text.service.text.generation;

import de.dhbw.woped.process2text.service.content.determination.labelAnalysis.EnglishLabelDeriver;
import de.dhbw.woped.process2text.service.content.determination.labelAnalysis.EnglishLabelHelper;
import de.dhbw.woped.process2text.service.content.determination.preprocessing.FormatConverter;
import de.dhbw.woped.process2text.service.content.determination.preprocessing.RigidStructurer;
import de.dhbw.woped.process2text.model.dsynt.DSynTSentence;
import de.dhbw.woped.process2text.model.pnmlReader.PNMLReader;
import de.dhbw.woped.process2text.model.pnmlReader.PetriNet.PetriNet;
import de.dhbw.woped.process2text.model.pnmlReader.PetriNetToProcessConverter;
import de.dhbw.woped.process2text.model.process.ProcessModel;
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

public class TextGenerator {
  ////////////////////////////////////
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
    PNMLReader pnmlReader = new PNMLReader();
    PetriNet petriNet = pnmlReader.getPetriNetFromPNMLString(is);
    PetriNetToProcessConverter pnConverter = new PetriNetToProcessConverter();
    ProcessModel model = pnConverter.convertToProcess(petriNet);

    // check number splits/joins
    pnConverter.printConversion();

    HashMap<Integer, String> transformedElemsRev = pnConverter.transformedElemsRev;

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
  }
}