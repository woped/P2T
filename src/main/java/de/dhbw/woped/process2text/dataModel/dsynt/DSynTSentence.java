package de.dhbw.woped.process2text.dataModel.dsynt;

import org.w3c.dom.Document;
import de.dhbw.woped.process2text.dataModel.intermediate.ExecutableFragment;

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