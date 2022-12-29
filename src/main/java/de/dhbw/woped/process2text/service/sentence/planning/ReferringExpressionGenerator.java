package de.dhbw.woped.process2text.service.sentence.planning;

import de.dhbw.woped.process2text.model.dsynt.DSynTMainSentence;
import de.dhbw.woped.process2text.model.dsynt.DSynTSentence;
import de.dhbw.woped.process2text.model.intermediate.ExecutableFragment;
import de.dhbw.woped.process2text.service.content.determination.label_analysis.EnglishLabelHelper;
import java.util.ArrayList;
import java.util.List;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.data.list.PointerTargetTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReferringExpressionGenerator {

  Logger logger = LoggerFactory.getLogger(ReferringExpressionGenerator.class);
  private final EnglishLabelHelper lHelper;
  public String currRole;

  public List<String> getRoleListRef() {
    return roleListRef;
  }

  private List<String> roleListRef;

  public ReferringExpressionGenerator(EnglishLabelHelper lHelper) {
    this.lHelper = lHelper;
  }

  public ArrayList<DSynTSentence> insertReferringExpressions(
      ArrayList<DSynTSentence> textPlan, boolean male) {
    String prevRole = null;
    ExecutableFragment prevFragment = null;
    DSynTSentence prevSentence = null;

    for (DSynTSentence aTextPlan : textPlan) {
      ExecutableFragment currFragment = aTextPlan.getExecutableFragment();

      if (prevRole != null && prevFragment != null) {

        if (currRole.equals(prevRole)
            && !currRole.equals("")
            && !currRole.equals("he")
            && !currRole.equals("she")
            && !currRole.equals("it")
            && !currFragment.senHasBullet
            && currFragment.senLevel == prevFragment.senLevel
            && prevSentence.getExecutableFragment().getListSize() == 0
            && !currFragment.senHasConnective
            && !prevFragment.senHasConnective
            && aTextPlan.getClass().toString().endsWith("DSynTMainSentence")
            && prevSentence.getClass().toString().endsWith("DSynTMainSentence")) {

          // Insert referring expression
          if (isPerson(currRole)) {
            if (male) {
              aTextPlan.getExecutableFragment().setRole("he");
            } else {
              aTextPlan.getExecutableFragment().setRole("she");
            }
          } else {
            aTextPlan.getExecutableFragment().setRole("it");
          }

          ((DSynTMainSentence) aTextPlan).changeRole();
          logger.info(
              "Referring Expression inserted: "
                  + aTextPlan.getExecutableFragment().getAction()
                  + " - "
                  + aTextPlan.getExecutableFragment().getBo());
          prevRole = null;
          prevFragment = null;
          prevSentence = null;
        }
      } else {
        prevRole = currRole;
        prevFragment = currFragment;
        prevSentence = aTextPlan;
      }
    }
    return textPlan;
  }

  public List<String> generateRoleList(ArrayList<DSynTSentence> textPlan) {
    List<String> roleListRef = new ArrayList<>();
    for (DSynTSentence aTextPlan : textPlan) {
      currRole = aTextPlan.getExecutableFragment().getRole();
      int i = 0;
      int j = 0;
      if (currRole.isEmpty()) {
        i = i + 1;
      } else {
        j = j + 1;
        roleListRef.add(currRole);
      }
    }
    return roleListRef;
  }

  // Checks WordNet HypernymTree whether "role" is a person
  private boolean isPerson(String role) {
    try {
      IndexWord word = lHelper.getDictionary().getIndexWord(POS.NOUN, role.toLowerCase());
      if (word != null) {
        Synset[] senses = word.getSenses();
        for (Synset sense : senses) {
          PointerTargetTree relatedTree = PointerUtils.getInstance().getHypernymTree(sense);
          PointerTargetNodeList[] relatedLists = relatedTree.reverse();
          for (PointerTargetNodeList relatedList : relatedLists) {
            for (Object aRelatedList : relatedList) {
              PointerTargetNode elem = (PointerTargetNode) aRelatedList;
              Synset syns = elem.getSynset();
              for (int j = 0; j < syns.getWords().length; j++) {
                if (syns.getWord(j).getLemma().equals("person")) {
                  return true;
                }
              }
            }
          }
        }
      }
    } catch (JWNLException e) {
      logger.error(e.getLocalizedMessage());
      return false;
    }
    return false;
  }
}
