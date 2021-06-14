package de.dhbw.woped.process2text.P2TWebservice;

import de.dhbw.woped.process2text.textGenerator.TextGenerator;

public class P2TController extends Thread {
    // This is the controller class, which is being called by the class P2T Servlet
    // Right now only consists of one return and one generateText Method
    // generate Text calls the main function in TextGenerator to translate a petri net to
    // natural language
    String text;
    public static final int MAX_INPUT_LENGTH=15000;//Reject any Request larger than this

    public String generateText(String text) {
        this.text = text;
        String output = "";
        if(text.contains("woped.org")) { //"woped.org" needs to be part of a Woped pnml File
            TextGenerator tg = new TextGenerator();

            try {
                output = tg.toText(text, true);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("BPMN");
            /** BPMN Reader Call **/
        }
        return output;
    }

    public String getText() {
        return text;
    }
}
