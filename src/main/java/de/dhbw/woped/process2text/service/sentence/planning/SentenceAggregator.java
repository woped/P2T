package de.dhbw.woped.process2text.service.sentence.planning;

import de.dhbw.woped.process2text.model.dsynt.DSynTMainSentence;
import de.dhbw.woped.process2text.model.dsynt.DSynTSentence;
import de.dhbw.woped.process2text.model.intermediate.ExecutableFragment;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SentenceAggregator {

  Logger logger = LoggerFactory.getLogger(SentenceAggregator.class);

  public ArrayList<DSynTSentence> performRoleAggregation(ArrayList<DSynTSentence> textPlan) {
    ArrayList<Integer> toBeDeleted = new ArrayList<>();

    String prevRole = null;
    ExecutableFragment prevFragment = null;
    DSynTSentence prevSentence = null;
    int deleteCount = 0;

    for (int i = 0; i < textPlan.size(); i++) {
      // Determine current role
      String currRole = textPlan.get(i).getExecutableFragment().getRole();
      ExecutableFragment currFragment = textPlan.get(i).getExecutableFragment();
      DSynTSentence currSentence = textPlan.get(i);

      if (i > 1 && prevRole != null && prevFragment != null && prevSentence != null) {

        if (currRole.equals(prevRole)
            && !currRole.equals("")
            && !currFragment.sen_hasBullet
            && currFragment.sen_level == prevFragment.sen_level
            && prevSentence.getExecutableFragment().getListSize() == 0
            && !currFragment.sen_hasConnective
            && !prevFragment.sen_hasConnective
            && currSentence.getClass().toString().equals("class DSynTMainSentence")
            && prevSentence.getClass().toString().equals("class DSynTMainSentence")) {

          // Create list with sentences which need to be aggregated with the current one
          ArrayList<DSynTMainSentence> coordSentences = new ArrayList<>();
          coordSentences.add((DSynTMainSentence) currSentence);

          // Conduct role aggregation
          ((DSynTMainSentence) prevSentence).addCoordSentences(coordSentences);
          toBeDeleted.add(i - deleteCount);
          deleteCount++;

          logger.info(
              "Aggregated: "
                  + textPlan.get(i).getExecutableFragment().getAction()
                  + " - "
                  + textPlan.get(i).getExecutableFragment().getBo());
          prevRole = null;
          prevFragment = null;
          prevSentence = null;
        }
      } else {
        prevRole = currRole;
        prevFragment = currFragment;
        prevSentence = currSentence;
      }
    }
    for (int i : toBeDeleted) {
      textPlan.remove(i);
    }
    return textPlan;
  }
}
