package de.dhbw.woped.process2text.P2TWebservice;

import de.dhbw.woped.process2text.textGenerator.TextGenerator;

public class P2TController extends Thread {
  // This is the controller class, which is being called by the class P2T Servlet
  // Right now only consists of one return and one generateText Method
  // generate Text calls the main function in TextGenerator to translate a petri net to
  // natural language
  String text;
  public static final int MAX_INPUT_LENGTH = 15000; // Reject any Request larger than this

  public String generateText(String text) {
    this.text = prepareText(text);
    String output = "";
    TextGenerator tg = new TextGenerator();

    try {
      output = tg.toText(this.text, true);

    } catch (Exception e) {
      e.printStackTrace();
    }
    return output;
  }

  /**
   * In case the pnml in send via an html form, it starts with the name of the input form. For
   * further processing, this part must be removed.
   *
   * @param pnml the pnml
   * @return a valid xml
   */
  private String prepareText(String pnml) {
    pnml = pnml.trim();
    return pnml.startsWith("=") ? pnml.substring(1) : pnml;
  }

  public String getText() {
    return text;
  }
}
