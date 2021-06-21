package de.dhbw.woped.process2text.pnmlProcessing.dataModel.dsynt;

import org.w3c.dom.Document;
import de.dhbw.woped.process2text.pnmlProcessing.dataModel.intermediate.ExecutableFragment;

public abstract class DSynTSentence {
    Document doc;
    ExecutableFragment eFrag;

    public Document getDSynT() {
        return doc;
    }

    public ExecutableFragment getExecutableFragment() {
        return eFrag;
    }
}