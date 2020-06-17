package org.woped.p2t.textPlanning.recordClasses;

import org.woped.p2t.dataModel.dsynt.DSynTConditionSentence;
import org.woped.p2t.dataModel.dsynt.DSynTSentence;
import org.woped.p2t.dataModel.intermediate.ConditionFragment;

import java.util.ArrayList;

public class ConverterRecord {
    public final ArrayList<DSynTSentence> preStatements;
    public final ArrayList<DSynTSentence> postStatements;
    public final ConditionFragment pre;
    public final ConditionFragment post;
    public ModifierRecord mod;
    public DSynTConditionSentence fullStatements;

    public ConverterRecord(ConditionFragment pre, ConditionFragment post, ArrayList<DSynTSentence> preStatements, ArrayList<DSynTSentence> postStatements) {
        this.pre = pre;
        this.post = post;
        this.preStatements = preStatements;
        this.postStatements = postStatements;
    }

    public ConverterRecord(ConditionFragment pre, ConditionFragment post, ArrayList<DSynTSentence> preStatements, ArrayList<DSynTSentence> postStatements, ModifierRecord mod) {
        this.pre = pre;
        this.post = post;
        this.preStatements = preStatements;
        this.postStatements = postStatements;
        this.mod = mod;
    }
    public ConverterRecord(ConditionFragment pre, ConditionFragment post, ArrayList<DSynTSentence> preStatements, ArrayList<DSynTSentence> postStatements, ModifierRecord mod, DSynTConditionSentence fullStatements) {
        this.pre = pre;
        this.post = post;
        this.preStatements = preStatements;
        this.postStatements = postStatements;
        this.mod = mod;
        this.fullStatements = fullStatements;
    }

    public boolean hasPreStatements() {
        return preStatements != null;
    }
}