/* (C)2022 */
package de.dhbw.woped.process2text.dataModel.dsynt;

import de.dhbw.woped.process2text.dataModel.intermediate.ExecutableFragment;
import org.w3c.dom.Document;

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
