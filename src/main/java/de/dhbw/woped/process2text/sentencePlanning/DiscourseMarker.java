package de.dhbw.woped.process2text.sentencePlanning;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import de.dhbw.woped.process2text.dataModel.dsynt.DSynTConditionSentence;
import de.dhbw.woped.process2text.dataModel.dsynt.DSynTMainSentence;
import de.dhbw.woped.process2text.dataModel.dsynt.DSynTSentence;
import de.dhbw.woped.process2text.textPlanning.IntermediateToDSynTConverter;

import java.util.ArrayList;

public class DiscourseMarker {
    private final ArrayList<String> SEQ_CONNECTIVES = new ArrayList<String>() {{
        add("afterwards");
        add("subsequently");
        add("next");
        add("latterly");
        add("thereafter");
        add("after that");
        add("consequently");
        add("following");
        add("thereon");
        add("later on");
        add("hereafter");
    }};

    public ArrayList<DSynTSentence> insertSequenceConnectives(ArrayList<DSynTSentence> textPlan) {
        int index = 0;
        int indexConnectors = 0;
        boolean inserted = false;
        for (DSynTSentence s : textPlan) {
            if (s instanceof DSynTConditionSentence) {
                DSynTConditionSentence condS = (DSynTConditionSentence) s;
                if (!condS.getExecutableFragment().sen_hasConnective && index > 0 && !condS.getConditionFragment().sen_headPosition) {
                    Element verb = condS.getVerb();
                    Document doc = condS.getDSynT();
                    IntermediateToDSynTConverter.insertConnective(doc, verb, SEQ_CONNECTIVES.get(indexConnectors));
                    inserted = true;
                }
            }
            if (s instanceof DSynTMainSentence) {
                DSynTMainSentence mainS = (DSynTMainSentence) s;
                if (!mainS.getExecutableFragment().sen_hasConnective && index > 0 && !mainS.getExecutableFragment().sen_hasBullet) {
                    Element verb = mainS.getVerb();
                    Document doc = mainS.getDSynT();

                    if(mainS.getExecutableFragment().getBo().equals("branch") && mainS.getExecutableFragment().getAction().equals("finish")){
                        IntermediateToDSynTConverter.insertConnective(doc, verb, "Then");
                    }else {
                        IntermediateToDSynTConverter.insertConnective(doc, verb, SEQ_CONNECTIVES.get(indexConnectors));
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