package de.dhbw.woped.process2text.pnmlProcessing.sentenceRealization;

import com.cogentex.real.api.RealProMgr;
import org.w3c.dom.Document;
import de.dhbw.woped.process2text.pnmlProcessing.dataModel.dsynt.DSynTConditionSentence;
import de.dhbw.woped.process2text.pnmlProcessing.dataModel.dsynt.DSynTMainSentence;
import de.dhbw.woped.process2text.pnmlProcessing.dataModel.dsynt.DSynTSentence;
import de.dhbw.woped.process2text.pnmlProcessing.dataModel.intermediate.ConditionFragment;
import de.dhbw.woped.process2text.pnmlProcessing.dataModel.intermediate.ExecutableFragment;

import java.util.ArrayList;
import java.util.HashMap;

public class SurfaceRealizer {
    private final RealProMgr realproManager;
    private int prevSenLvl;

    public SurfaceRealizer() {
        realproManager = new RealProMgr();
        prevSenLvl = 0;
    }

    public String realizeSentenceMap(ArrayList<DSynTSentence> sentencePlan, HashMap<Integer, String> map) {
        StringBuilder s = new StringBuilder("<text>\n");
        for (DSynTSentence dsynt : sentencePlan) {
            s.append(" ").append(realizeMapSentence(dsynt, map)).append("\n");
        }
        return s + "</text>";
    }

    private String realizeMapSentence(DSynTSentence s, HashMap<Integer, String> map) {
        Document xmldoc = s.getDSynT();
        realproManager.realize(xmldoc);
        ArrayList<Integer> ids = s.getExecutableFragment().getAssociatedActivities();
        if (s.getClass().toString().endsWith("DSynTConditionSentence")) {
            DSynTConditionSentence cs = (DSynTConditionSentence) s;
            ids.addAll(cs.getConditionFragment().getAssociatedActivities());
            ArrayList<ConditionFragment> sentences = cs.getConditionFragment().getSentenceList();
            if (sentences != null) {
                for (ConditionFragment cFrag : sentences) {
                    ids.addAll(cFrag.getAssociatedActivities());
                }
            }
        } else {
            DSynTMainSentence ms = (DSynTMainSentence) s;
            ArrayList<ExecutableFragment> sentences = ms.getExecutableFragment().getSentencList();
            if (sentences != null) {
                for (ExecutableFragment eFrag : sentences) {
                    ids.addAll(eFrag.getAssociatedActivities());
                }
            }
        }
        String output = "";
        StringBuilder idAttr = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                idAttr.append(",");
            }
            idAttr.append(map.get(ids.get(i)));
        }

        /* Sentence Level Depth visualization through XML List; Woped Frontend Parser needs to be adjusted
        if(s.getExecutableFragment().sen_level == prevSenLvl && s.getExecutableFragment().sen_level > 0) {
            return output + "<phrase ids=\"" + idAttr + "\">" +  "<item>" + realproManager.getSentenceString() + "</item> </phrase>";
        } else if(s.getExecutableFragment().sen_level > prevSenLvl) {
            prevSenLvl++;
            return output + "<list type=\"bullet\"><item><phrase ids=\"" + idAttr + "\">" + realproManager.getSentenceString() + " </phrase></item>";
        } else if(s.getExecutableFragment().sen_level < prevSenLvl){
            prevSenLvl--;
            if(s.getExecutableFragment().sen_level == 0){
                return output + "</list><phrase ids=\"" + idAttr + "\">" + realproManager.getSentenceString() + " </phrase>";
            }
            return output + "</list><item><phrase ids=\"" + idAttr + "\">" + realproManager.getSentenceString() + " </phrase></item>";
        }
        */

        if(s.getExecutableFragment().sen_level > 0) {
            String spacer = "";
            for(int i = 0; i < s.getExecutableFragment().sen_level; i++){
                spacer += "&#032";
            }
            if(s.getExecutableFragment().sen_level%2 == 0){
                spacer += "&#149";
            } else {
                spacer += "-";
            }

            return output + "<phrase ids=\"" + idAttr + "\">" + spacer + realproManager.getSentenceString() + " </phrase>"; //Bulletpoints and margin according to sentence level
        }
        return output + "<phrase ids=\"" + idAttr + "\"> " + realproManager.getSentenceString() + " </phrase>";
    }

    public String postProcessText(String surfaceText) {
        surfaceText = surfaceText.replaceAll("If it is necessary", "If it is necessary,");
        surfaceText = surfaceText.replaceAll("one of the branches was executed", "one of the branches was executed,");
        surfaceText = surfaceText.replaceAll("In concurrency to the latter steps", "In concurrency to the latter steps,");
        surfaceText = surfaceText.replaceAll("Once both branches were finished", "Once both branches were finished,");
        surfaceText = surfaceText.replaceAll("Once the loop is finished", "Once the loop is finished,");
        surfaceText = surfaceText.replaceAll("one of the following branches is executed.", "one of the following branches is executed:");
        surfaceText = surfaceText.replaceAll("one or more of the following branches is executed.", "one or more of the following branches is executed:");
        surfaceText = surfaceText.replaceAll("parallel branches.", "parallel branches:");
        surfaceText = surfaceText.replaceAll("The process begins", "The process begins,");
        surfaceText = surfaceText.replaceAll("If it is required", "If it is required,");
        surfaceText = surfaceText.replaceAll(" the a ", " a ");
        surfaceText = surfaceText.replaceAll("branches were executed ", "branches were executed, ");

        return surfaceText;
    }
}