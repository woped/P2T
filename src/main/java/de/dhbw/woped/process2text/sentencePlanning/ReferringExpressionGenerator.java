/* (C)2022 */
package de.dhbw.woped.process2text.sentencePlanning;

import de.dhbw.woped.process2text.contentDetermination.labelAnalysis.EnglishLabelHelper;
import de.dhbw.woped.process2text.dataModel.dsynt.DSynTMainSentence;
import de.dhbw.woped.process2text.dataModel.dsynt.DSynTSentence;
import de.dhbw.woped.process2text.dataModel.intermediate.ExecutableFragment;
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

public class ReferringExpressionGenerator {
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
      // Determine current role
      //            roleListRef = generateRoleList(aTextPlan);
      // roleListRef.add(currRole);

      ExecutableFragment currFragment = aTextPlan.getExecutableFragment();

      if (prevRole != null && prevFragment != null) {

        if (currRole.equals(prevRole)
            && !currRole.equals("")
            && !currRole.equals("he")
            && !currRole.equals("she")
            && !currRole.equals("it")
            && !currFragment.sen_hasBullet
            && currFragment.sen_level == prevFragment.sen_level
            && prevSentence.getExecutableFragment().getListSize() == 0
            && !currFragment.sen_hasConnective
            && !prevFragment.sen_hasConnective
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
          System.out.println(
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
      e.printStackTrace();
      return false;
    }
    return false;
  }
}
