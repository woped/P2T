package de.dhbw.woped.process2text.P2TWebservice;

import de.dhbw.woped.process2text.bpmnProcessing.BPMNReader.XMLReader;
import de.dhbw.woped.process2text.bpmnProcessing.BPMNStart;
import de.dhbw.woped.process2text.bpmnProcessing.dataModel.jsonStructure.CamundaMapper;
import de.dhbw.woped.process2text.pnmlProcessing.textGenerator.TextGenerator;
import net.didion.jwnl.JWNLException;

import java.io.IOException;

public class P2TController extends Thread {
    // This is the controller class, which is being called by the class P2T Servlet
    // Right now only consists of one return and one generateText Method
    // generate Text calls the main function in TextGenerator to translate a petri net to
    // natural language
    String text;
    public static final int MAX_INPUT_LENGTH=15000;//Reject any Request larger than this

    /**
     * Decides wheter Input is PNML or BPMN and calls corresponding Text Generator. Note: PNML files must Contain "woped.org" in their header.
     * @param text
     * @return Natural Language interpretation of Input.
     */
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
        } else if (text.contains("Camunda")){
            /** BPMN Reader Call **/
            //System.out.println(XMLReader.read(text));

            try {
                BPMNStart bpmn = new BPMNStart();
                CamundaMapper mapper = new CamundaMapper();
                String json = XMLReader.read(text);
                output = bpmn.createText(mapper.buildDoc(json));
                //output = bpmn.createFromFile(text);
            } catch (JWNLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            output = "unknown file format";
        }
        return output;
    }

    public String getText() {
        return text;
    }
}
