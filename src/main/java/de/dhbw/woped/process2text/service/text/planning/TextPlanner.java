package de.dhbw.woped.process2text.service.text.planning;

import de.dhbw.woped.process2text.model.Pair;
import de.dhbw.woped.process2text.model.dsynt.DSynTConditionSentence;
import de.dhbw.woped.process2text.model.dsynt.DSynTMainSentence;
import de.dhbw.woped.process2text.model.dsynt.DSynTSentence;
import de.dhbw.woped.process2text.model.intermediate.AbstractFragment;
import de.dhbw.woped.process2text.model.intermediate.ConditionFragment;
import de.dhbw.woped.process2text.model.intermediate.ExecutableFragment;
import de.dhbw.woped.process2text.model.process.ProcessModel;
import de.dhbw.woped.process2text.service.content.determination.extraction.GatewayExtractor;
import de.dhbw.woped.process2text.service.content.determination.label_analysis.EnglishLabelDeriver;
import de.dhbw.woped.process2text.service.content.determination.label_analysis.EnglishLabelHelper;
import de.dhbw.woped.process2text.service.content.determination.preprocessing.FormatConverter;
import de.dhbw.woped.process2text.service.text.planning.recordClasses.ConverterRecord;
import de.dhbw.woped.process2text.service.text.planning.recordClasses.GatewayPropertyRecord;
import de.dhbw.woped.process2text.service.text.planning.recordClasses.ModifierRecord;
import de.hpi.bpt.graph.algo.rpst.RPST;
import de.hpi.bpt.graph.algo.rpst.RPSTNode;
import de.hpi.bpt.process.ControlFlow;
import de.hpi.bpt.process.Node;
import de.hpi.bpt.process.Process;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import net.didion.jwnl.JWNLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextPlanner {

  Logger logger = LoggerFactory.getLogger(TextPlanner.class);

  private static final String[] quantifiers = {
    "a", "the", "all", "any", "more", "most", "none", "some", "such", "one", "two", "three", "four",
    "five", "six", "seven", "eight", "nine", "ten"
  };
  private final RPST<ControlFlow, Node> rpst;
  private final ProcessModel process;
  private final TextToIntermediateConverter textToIMConverter;
  private final ArrayList<ConditionFragment> passedFragments;
  private final ArrayList<ModifierRecord> passedMods; // used for Skips
  private final boolean isAlternative;
  private final ArrayList<DSynTSentence> sentencePlan;
  private final ArrayList<Pair<Integer, DSynTSentence>> activitiySentenceMap;
  private final EnglishLabelHelper lHelper;
  private final EnglishLabelDeriver lDeriver;
  private final boolean imperative;
  private final String imperativeRole;
  private ModifierRecord passedMod = null; // used for AND-Splits
  private boolean tagWithBullet = false;
  private boolean start = true;

  public TextPlanner(
      RPST<ControlFlow, Node> rpst,
      ProcessModel process,
      EnglishLabelDeriver lDeriver,
      EnglishLabelHelper lHelper,
      String imperativeRole,
      boolean imperative,
      boolean isAlternative) {
    this.rpst = rpst;
    this.process = process;
    this.lHelper = lHelper;
    this.lDeriver = lDeriver;
    textToIMConverter =
        new TextToIntermediateConverter(rpst, process, lHelper, imperativeRole, imperative);
    passedFragments = new ArrayList<>();
    sentencePlan = new ArrayList<>();
    activitiySentenceMap = new ArrayList<>();
    passedMods = new ArrayList<>();
    this.imperative = imperative;
    this.imperativeRole = imperativeRole;
    this.isAlternative = isAlternative;
  }

  /** Text Planning Main */
  public void convertToText(RPSTNode<ControlFlow, Node> root, int level)
      throws JWNLException, FileNotFoundException {
    String passRole = null;
    if (root == null) {
      return;
    }

    // Order nodes of current level with respect to control flow
    ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes =
        PlanningHelper.sortTreeLevel(root, root.getEntry(), rpst);

    // For each node of current level
    for (RPSTNode<ControlFlow, Node> node : orderedTopNodes) {

      if (PlanningHelper.isEvent(node.getExit())) {
        orderedTopNodes.indexOf(node);
        orderedTopNodes.size();
      }
      int depth = PlanningHelper.getDepth(node, rpst);
      if (PlanningHelper.isBond(node)) {

        // Converter Record
        ConverterRecord convRecord = null;

        // **************************************  LOOP - SPLIT
        // **************************************
        if (PlanningHelper.isLoop(node, rpst)) {
          convRecord = getLoopConverterRecord(node);
        }
        // **************************************  SKIP - SPLIT
        // **************************************
        if (PlanningHelper.isSkip(node, rpst)) {
          convRecord = getSkipConverterRecord(orderedTopNodes, node);
        }
        // **************************************  XOR - SPLIT
        // **************************************
        if (PlanningHelper.isXORSplit(node, rpst)) {
          convRecord = getXORConverterRecord(node);
        }
        // **************************************  EVENT BASED - SPLIT
        // **************************************
        if (PlanningHelper.isEventSplit(node, rpst)) {
          convRecord = getXORConverterRecord(node);
        }
        // **************************************  OR - SPLIT
        // **************************************
        if (PlanningHelper.isORSplit(node, rpst)) {
          convRecord = getORConverterRecord(node);
        }
        // **************************************  AND - SPLIT
        // **************************************
        if (PlanningHelper.isANDSplit(node, rpst)) {
          convRecord = getANDConverterRecord(node);
        }

        // Add pre statements
        if (convRecord != null && convRecord.preStatements != null) {
          for (DSynTSentence preStatement : convRecord.preStatements) {
            if (passedFragments.size() > 0) {
              DSynTConditionSentence dsyntSentence =
                  new DSynTConditionSentence(
                      preStatement.getExecutableFragment(), passedFragments.get(0));
              if (passedFragments.size() > 1) {
                for (int i = 1; i < passedFragments.size(); i++) {
                  dsyntSentence.addCondition(passedFragments.get(i), true);
                  dsyntSentence.getConditionFragment().addCondition(passedFragments.get(i));
                }
              }
              passedFragments.clear();
              sentencePlan.add(dsyntSentence);
            } else {
              preStatement.getExecutableFragment().senLevel = level;
              if (passedMods.size() > 0) {
                preStatement
                    .getExecutableFragment()
                    .addMod(passedMods.get(0).getLemma(), passedMods.get(0));
                preStatement.getExecutableFragment().senHasConnective = true;
                passedMods.clear();
              }
              sentencePlan.add(new DSynTMainSentence(preStatement.getExecutableFragment()));
              start = false;
            }
          }
        }

        // Pass precondition
        if (convRecord != null && convRecord.pre != null) {
          if (passedFragments.size() > 0) {
            if (passedFragments.get(0).getFragmentType() == AbstractFragment.TYPE_JOIN) {
              ExecutableFragment eFrag = new ExecutableFragment("continue", "process", "", "");
              eFrag.boIsSubject = true;
              DSynTConditionSentence dsyntSentence =
                  new DSynTConditionSentence(eFrag, passedFragments.get(0));
              sentencePlan.add(dsyntSentence);
              passedFragments.clear();
            }
          }
          passedFragments.add(convRecord.pre);
        }

        // Convert to Text
        // LOOP
        if (PlanningHelper.isLoop(node, rpst) || PlanningHelper.isSkip(node, rpst)) {
          convertToText(node, level + 1);
        }
        // XOR - OR - Event
        if (PlanningHelper.isXORSplit(node, rpst)
            || PlanningHelper.isORSplit(node, rpst)
            || PlanningHelper.isEventSplit(node, rpst)) {
          ArrayList<RPSTNode<ControlFlow, Node>> paths =
              PlanningHelper.sortTreeLevel(node, node.getEntry(), rpst);
          for (RPSTNode<ControlFlow, Node> path : paths) {
            tagWithBullet = true;
            convertToText(path, level + 1);
          }
        }
        // AND
        if (PlanningHelper.isANDSplit(node, rpst)) {

          // Determine path count
          ArrayList<RPSTNode<ControlFlow, Node>> andNodes =
              PlanningHelper.sortTreeLevel(node, node.getEntry(), rpst);

          if (andNodes.size() == 2) {
            ArrayList<RPSTNode<ControlFlow, Node>> topNodes =
                PlanningHelper.sortTreeLevel(node, node.getEntry(), rpst);
            RPSTNode<ControlFlow, Node> path1 = topNodes.get(0);
            RPSTNode<ControlFlow, Node> path2 = topNodes.get(1);

            // Convert both paths
            convertToText(path1, level);
            passedMod = convRecord.mod;
            convertToText(path2, level);
          } else {
            ArrayList<RPSTNode<ControlFlow, Node>> paths =
                PlanningHelper.sortTreeLevel(node, node.getEntry(), rpst);
            for (RPSTNode<ControlFlow, Node> path : paths) {
              tagWithBullet = true;
              convertToText(path, level + 1);
            }
          }
        }

        // Add post statement to sentence plan
        if (convRecord != null && convRecord.postStatements != null) {
          sentencePlan.addAll(convRecord.postStatements);
        }

        // Pass post fragment
        if (convRecord != null && convRecord.post != null) {
          passedFragments.add(convRecord.post);
        }
        // **************************************  ACTIVITIES
        // **************************************
      } else if (PlanningHelper.isTask(node.getEntry())) {
        convertActivities(node, level, depth);

        // Handle End Event
        if (PlanningHelper.isEvent(node.getExit())) {
          de.dhbw.woped.process2text.model.process.Event event =
              process.getEvents().get((Integer.valueOf(node.getExit().getId())));
          if (event.getType() == de.dhbw.woped.process2text.model.process.EventType.END_EVENT
              && orderedTopNodes.indexOf(node) == orderedTopNodes.size() - 1) {
            // Adjust level and add to sentence plan
            DSynTSentence sen = textToIMConverter.convertEvent(event).preStatements.get(0);
            sen.getExecutableFragment().senLevel = level;
            sentencePlan.add(sen);
          }
        }
      }
      // **************************************  EVENTS  **************************************
      //			 else if (PlanningHelper.isEvent(node.getEntry()) && orderedTopNodes.indexOf(node) > 0) {
      else if (PlanningHelper.isEvent(node.getEntry())) {
        de.dhbw.woped.process2text.model.process.Event event =
            process.getEvents().get((Integer.valueOf(node.getEntry().getId())));
        int currentPosition = orderedTopNodes.indexOf(node);
        // Start Event
        if (currentPosition == 0) {

          // Start event should be printed
          if (start && !isAlternative) {

            // Event is followed by gateway --> full sentence
            if (event.getType() == de.dhbw.woped.process2text.model.process.EventType.START_EVENT
                && currentPosition < orderedTopNodes.size() - 1
                && PlanningHelper.isBond(orderedTopNodes.get(currentPosition + 1))) {
              start = false;
              ExecutableFragment eFrag =
                  new ExecutableFragment("start", "process", "", "with a decision");
              eFrag.addHasArticle = false;
              eFrag.boIsSubject = true;
              sentencePlan.add(new DSynTMainSentence(eFrag));
            }
            if (event.getType() != de.dhbw.woped.process2text.model.process.EventType.START_EVENT) {
              start = false;
              ConverterRecord convRecord = textToIMConverter.convertEvent(event);
              if (convRecord != null && convRecord.hasPreStatements()) {
                sentencePlan.add(convRecord.preStatements.get(0));
              }
            }
          }

          // Intermediate Events
        } else {
          ConverterRecord convRecord = textToIMConverter.convertEvent(event);

          // Add fragments if applicable
          if (convRecord != null && convRecord.pre != null) {
            passedFragments.add(convRecord.pre);
          }

          // Adjust level and add to sentence plan (first sentence not indented)
          if (convRecord != null && convRecord.hasPreStatements()) {
            for (int i = 0; i < convRecord.preStatements.size(); i++) {

              DSynTSentence sen = convRecord.preStatements.get(i);

              // If only one sentence (e.g. "Intermediate" End Event)
              if (convRecord.preStatements.size() == 1) {
                sen.getExecutableFragment().senLevel = level;
              }

              if (tagWithBullet) {
                sen.getExecutableFragment().senHasBullet = true;
                sen.getExecutableFragment().senLevel = level;
                tagWithBullet = false;
              }

              if (i > 0) {
                sen.getExecutableFragment().senLevel = level;
              }
              sentencePlan.add(sen);
            }
          }
        }
      } else {
        if (depth > 0) {
          convertToText(node, level);
        }
      }
    }
  }
  //////////////////////////////////////////////////////////////////////////////////////////////
  public String testGetRole() {
    return role;
  }

  String role;

  private void convertActivities(RPSTNode<ControlFlow, Node> node, int level, int depth)
      throws JWNLException, FileNotFoundException {

    boolean planned = false;

    de.dhbw.woped.process2text.model.process.Activity activity =
        process.getActivity(Integer.parseInt(node.getEntry().getId()));
    de.dhbw.woped.process2text.model.process.Annotation anno = activity.getAnnotations().get(0);

    ExecutableFragment eFrag;

    // Start of the process
    if (start && !isAlternative) {

      start = false;
      ModifierRecord modRecord =
          new ModifierRecord(ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
      modRecord.addAttribute("starting_point", "+");

      String bo = anno.getBusinessObjects().get(0);
      eFrag = new ExecutableFragment(anno.getActions().get(0), bo, "", anno.getAddition());
      eFrag.addAssociation(activity.getId());
      eFrag.addMod("the process begins when", modRecord);

      role = getRole(activity, eFrag);
      eFrag.setRole(role);

      if (anno.getActions().size() == 2) {
        ExecutableFragment eFrag2;
        if (anno.getBusinessObjects().size() == 2) {
          eFrag2 =
              new ExecutableFragment(
                  anno.getActions().get(1), anno.getBusinessObjects().get(1), "", "");
          eFrag2.addAssociation(activity.getId());
        } else {
          eFrag2 = new ExecutableFragment(anno.getActions().get(1), "", "", "");
          eFrag2.addAssociation(activity.getId());
        }
        correctArticleSettings(eFrag2);
        eFrag.addSentence(eFrag2);
      }

      if (bo.endsWith("s") && lHelper.isNoun(bo.substring(0, bo.length() - 1))) {
        eFrag.boHasArticle = true;
      } else {
        eFrag.boHasIndefArticle = true;
      }

      // If imperative mode
      if (imperative && imperativeRole.equals(role)) {
        eFrag.verbIsImperative = true;
        eFrag.roleIsImperative = true;
      }
      correctArticleSettings(eFrag);
      DSynTMainSentence dsyntSentence = new DSynTMainSentence(eFrag);
      sentencePlan.add(dsyntSentence);
      activitiySentenceMap.add(new Pair<>(Integer.valueOf(node.getEntry().getId()), dsyntSentence));

      planned = true;
    }

    // Standard case
    eFrag =
        new ExecutableFragment(
            anno.getActions().get(0), anno.getBusinessObjects().get(0), "", anno.getAddition());
    eFrag.addAssociation(activity.getId());
    if (activity.getType()
        == de.dhbw.woped.process2text.model.process.ActivityType.TYPE_MAP.get("Subprocess")) {
      eFrag.setAddition("in a subprocess");
    }
    String role = getRole(activity, eFrag);
    eFrag.setRole(role);
    if (anno.getActions().size() == 2) {
      ExecutableFragment eFrag2;
      if (anno.getBusinessObjects().size() == 2) {
        eFrag2 =
            new ExecutableFragment(
                anno.getActions().get(1), anno.getBusinessObjects().get(1), "", "");
        if (eFrag.verbIsPassive) {
          if (anno.getBusinessObjects().get(0).equals("")) {
            eFrag2.verbIsPassive = true;
            eFrag.setBo(eFrag2.getBo());
            eFrag2.setBo("");
            eFrag.boHasArticle = true;
          } else {
            eFrag2.verbIsPassive = true;
            eFrag2.boIsSubject = true;
          }
        }
      } else {
        eFrag2 = new ExecutableFragment(anno.getActions().get(1), "", "", "");
        if (eFrag.verbIsPassive) {
          eFrag2.verbIsPassive = true;
        }
      }

      correctArticleSettings(eFrag2);
      eFrag2.addAssociation(activity.getId());
      eFrag.addSentence(eFrag2);
    }

    eFrag.senLevel = level;
    if (imperative && imperativeRole.equals(role)) {
      correctArticleSettings(eFrag);
      eFrag.verbIsImperative = true;
      eFrag.setRole("");
    }

    // In case of passed modifications (NOT AND - Split)
    if (passedMods.size() > 0 && !planned) {
      correctArticleSettings(eFrag);
      eFrag.addMod(passedMods.get(0).getLemma(), passedMods.get(0));
      eFrag.senHasConnective = true;
      passedMods.clear();
    }

    // In case of passed modifications (e.g. AND - Split)
    if (passedMod != null && !planned) {
      correctArticleSettings(eFrag);
      eFrag.addMod(passedMod.getLemma(), passedMod);
      eFrag.senHasConnective = true;
      passedMod = null;
    }

    if (tagWithBullet) {
      eFrag.senHasBullet = true;
      tagWithBullet = false;
    }

    // In case of passed fragments (General handling)
    if (passedFragments.size() > 0 && !planned) {
      correctArticleSettings(eFrag);
      DSynTConditionSentence dsyntSentence =
          new DSynTConditionSentence(eFrag, passedFragments.get(0));
      if (passedFragments.size() > 1) {
        for (int i = 1; i < passedFragments.size(); i++) {
          dsyntSentence.addCondition(passedFragments.get(i), true);
          dsyntSentence.getConditionFragment().addCondition(passedFragments.get(i));
        }
      }
      sentencePlan.add(dsyntSentence);
      activitiySentenceMap.add(new Pair<>(Integer.valueOf(node.getEntry().getId()), dsyntSentence));
      passedFragments.clear();
      planned = true;
    }

    if (!planned) {
      correctArticleSettings(eFrag);
      DSynTMainSentence dsyntSentence = new DSynTMainSentence(eFrag);
      sentencePlan.add(dsyntSentence);
      activitiySentenceMap.add(new Pair<>(Integer.valueOf(node.getEntry().getId()), dsyntSentence));
    }

    // If activity has attached Events
    if (activity.hasAttachedEvents()) {
      ArrayList<Integer> attachedEvents = activity.getAttachedEvents();
      HashMap<Integer, ProcessModel> alternativePaths = process.getAlternativePaths();
      for (Integer attEvent : attachedEvents) {
        if (alternativePaths.keySet().contains(attEvent)) {
          logger.info("Incorporating Alternative " + attEvent);
          // Transform alternative
          ProcessModel alternative = alternativePaths.get(attEvent);
          alternative.annotateModel(lDeriver, lHelper);

          // Consider complexity of the process
          if (alternative.getElemAmount() <= 3) {
            alternative.getEvents().get(attEvent).setLeadsToEnd(true);
          }

          FormatConverter rpstConverter = new FormatConverter();
          Process p = rpstConverter.transformToRPSTFormat(alternative);
          RPST<ControlFlow, Node> rpst = new RPST<>(p);
          TextPlanner converter =
              new TextPlanner(
                  rpst, alternative, lDeriver, lHelper, imperativeRole, imperative, true);
          PlanningHelper.printTree(rpst.getRoot(), 0, rpst);
          converter.convertToText(rpst.getRoot(), level + 1);
          ArrayList<DSynTSentence> subSentencePlan = converter.getSentencePlan();
          for (int i = 0; i < subSentencePlan.size(); i++) {
            DSynTSentence sen = subSentencePlan.get(i);
            if (i == 0) {
              sen.getExecutableFragment().senLevel = level;
            }
            if (i == 1) {
              sen.getExecutableFragment().senHasBullet = true;
            }
            sentencePlan.add(sen);
          }

          // Print sentence for subsequent normal execution
          sentencePlan.add(
              textToIMConverter.getAttachedEventPostStatement(
                  alternative.getEvents().get(attEvent)));
        }
      }
    }

    if (depth > 0) {
      convertToText(node, level);
    }
  }

  /** Get ConverterRecord for AND */
  private ConverterRecord getANDConverterRecord(RPSTNode<ControlFlow, Node> node) {
    // Determine path count
    ArrayList<RPSTNode<ControlFlow, Node>> andNodes =
        PlanningHelper.sortTreeLevel(node, node.getEntry(), rpst);
    /*
    if (andNodes.size() == 2) {

        // Determine last activities of the AND split paths
        ArrayList<Node> conditionNodes = new ArrayList<>();
        for (RPSTNode<ControlFlow, Node> n : andNodes) {
            ArrayList<RPSTNode<ControlFlow, Node>> pathNodes = PlanningHelper.sortTreeLevel(n, n.getEntry(), rpst);
            Node lastNode = pathNodes.get(pathNodes.size() - 1).getEntry();
            if (PlanningHelper.isTask(lastNode)) {
                conditionNodes.add(lastNode);
            }
        }
        //return textToIMConverter.convertANDSimple(node, PlanningHelper.getActivityCount(andNodes.get(0), rpst), conditionNodes);
        return textToIMConverter.convertANDGeneral(node, andNodes.size());
        // General case (paths > 2)
    } else {
        return textToIMConverter.convertANDGeneral(node, andNodes.size());
    }*/
    return textToIMConverter.convertANDGeneral(node, andNodes.size());
  }

  /** Get ConverterRecord for OR */
  private ConverterRecord getORConverterRecord(RPSTNode<ControlFlow, Node> node) {
    GatewayPropertyRecord orPropRec = new GatewayPropertyRecord(node, rpst);

    // Labeled Case
    if (orPropRec.isGatewayLabeled()) {
      return null;

      // Unlabeled case
    } else {
      return textToIMConverter.convertORSimple(node);
    }
  }

  /** Get ConverterRecord for XOR */
  private ConverterRecord getXORConverterRecord(RPSTNode<ControlFlow, Node> node) {
    GatewayPropertyRecord propRec = new GatewayPropertyRecord(node, rpst);

    // Labeled Case with Yes/No - arcs and Max. Depth of 1
    if (propRec.isGatewayLabeled() && propRec.hasYNArcs() && propRec.getMaxPathDepth() == 1) {
      GatewayExtractor gwExtractor = new GatewayExtractor(node.getEntry(), lHelper);

      // Add sentence
      sentencePlan.addAll(textToIMConverter.convertXORSimple(node, gwExtractor));
      return null;
      // General case
    } else {
      return textToIMConverter.convertXORGeneral(node);
    }
  }

  /** Get ConverterRecord for Loop */
  private ConverterRecord getLoopConverterRecord(RPSTNode<ControlFlow, Node> node) {
    RPSTNode<ControlFlow, Node> firstNodeInLoop = PlanningHelper.getNextActivity(node, rpst);
    return textToIMConverter.convertLoop(node, firstNodeInLoop);
  }

  /** Get ConverterRecord for Skip */
  private ConverterRecord getSkipConverterRecord(
      ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes, RPSTNode<ControlFlow, Node> node) {
    GatewayPropertyRecord propRec = new GatewayPropertyRecord(node, rpst);

    // Yes-No Case
    if (propRec.isGatewayLabeled() && propRec.hasYNArcs()) {

      // Yes-No Case which is directly leading to the end of the process
      if (isToEndSkip(orderedTopNodes, node)) {
        return textToIMConverter.convertSkipToEnd(node);

        // General Yes/No-Case
      } else {
        return textToIMConverter.convertSkipGeneral(node);
      }

      // General unlabeled Skip
    } else {
      return textToIMConverter.convertSkipGeneralUnlabeled(node);
    }
  }

  /** Evaluate whether skip leads to an end */
  private boolean isToEndSkip(
      ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes, RPSTNode<ControlFlow, Node> node) {
    int currentPosition = orderedTopNodes.indexOf(node);
    if (currentPosition < orderedTopNodes.size() - 1) {
      Node potEndNode = orderedTopNodes.get(currentPosition + 1).getExit();
      return PlanningHelper.isEndEvent(potEndNode, process);
    }
    return false;
  }

  /** Returns role of a fragment. */
  private String getRole(
      de.dhbw.woped.process2text.model.process.Activity a, AbstractFragment frag) {
    if (a.getLane() == null) {
      frag.verbIsPassive = true;
      frag.boIsSubject = true;
      if (frag.getBo().equals("")) {
        frag.setBo("it");
        frag.boHasArticle = false;
      }
      return "";
    }
    String role = a.getLane().getName();
    if (role.equals("")) {
      role = a.getPool().getName();
    }
    if (role.equals("")) {
      frag.verbIsPassive = true;
      frag.boIsSubject = true;
      if (frag.getBo().equals("")) {
        frag.setBo("it");
        frag.boHasArticle = false;
      }
    }
    return role;
  }

  /** Checks and corrects the article settings. */
  private void correctArticleSettings(AbstractFragment frag) {
    String bo = frag.getBo();
    if (bo.endsWith("s")
        && !bo.endsWith("ss")
        && frag.boHasArticle
        && lHelper.isNoun(bo.substring(0, bo.length() - 1))) {
      bo = bo.substring(0, bo.length() - 1);
      frag.setBo(bo);
      frag.boIsPlural = true;
    }
    if (bo.contains("&")) {
      frag.boIsPlural = true;
    }
    if (frag.boHasArticle) {
      String[] boSplit = bo.split(" ");
      if (boSplit.length > 1) {
        if (Arrays.asList(quantifiers).contains(boSplit[0].toLowerCase())) {
          frag.boHasArticle = false;
        }
      }
    }
    if (bo.equals("") && frag.boHasArticle) {
      frag.boHasArticle = false;
    }
    if (bo.startsWith("their") || bo.startsWith("a ") || bo.startsWith("for")) {
      frag.boHasArticle = false;
    }
    String[] splitAdd = frag.getAddition().split(" ");
    frag.addHasArticle =
        splitAdd.length <= 3 || !lHelper.isVerb(splitAdd[1]) || splitAdd[0].equals("on");
  }

  public ArrayList<DSynTSentence> getSentencePlan() {
    return sentencePlan;
  }
}
