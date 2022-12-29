package de.dhbw.woped.process2text.service.sentence.planning;

import de.dhbw.woped.process2text.model.dsynt.DSynTConditionSentence;
import de.dhbw.woped.process2text.model.dsynt.DSynTMainSentence;
import de.dhbw.woped.process2text.model.dsynt.DSynTSentence;
import de.dhbw.woped.process2text.service.text.planning.IntermediateToDSynTConverter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DiscourseMarker {
  private static final List<String> SEQ_CONNECTIVES =
      Arrays.asList(
          "afterwards",
          "subsequently",
          "next",
          "latterly",
          "thereafter",
          "after that",
          "consequently",
          "following",
          "thereon",
          "later on",
          "hereafter");

  public ArrayList<DSynTSentence> insertSequenceConnectives(ArrayList<DSynTSentence> textPlan) {
    int index = 0;
    int indexConnectors = 0;
    boolean inserted = false;
    for (DSynTSentence s : textPlan) {
      if (s instanceof DSynTConditionSentence) {
        DSynTConditionSentence condS = (DSynTConditionSentence) s;
        if (!condS.getExecutableFragment().senHasConnective
            && index > 0
            && !condS.getConditionFragment().senHeadPosition) {
          Element verb = condS.getVerb();
          Document doc = condS.getDSynT();
          IntermediateToDSynTConverter.insertConnective(
              doc, verb, SEQ_CONNECTIVES.get(indexConnectors));
          inserted = true;
        }
      }
      if (s instanceof DSynTMainSentence) {
        DSynTMainSentence mainS = (DSynTMainSentence) s;
        if (!mainS.getExecutableFragment().senHasConnective
            && index > 0
            && !mainS.getExecutableFragment().senHasBullet) {
          Element verb = mainS.getVerb();
          Document doc = mainS.getDSynT();

          if (mainS.getExecutableFragment().getBo().equals("branch")
              && mainS.getExecutableFragment().getAction().equals("finish")) {
            IntermediateToDSynTConverter.insertConnective(doc, verb, "Then");
          } else {
            IntermediateToDSynTConverter.insertConnective(
                doc, verb, SEQ_CONNECTIVES.get(indexConnectors));
          }
          inserted = true;
        }
      }

      // Adjust indices
      index++;
      if (inserted) {
        indexConnectors++;
        if (indexConnectors == SEQ_CONNECTIVES.size()) {
          indexConnectors = 0;
        }
        inserted = false;
      }
    }
    return textPlan;
  }
}
