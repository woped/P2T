package de.dhbw.woped.process2text.bpmnProcessing.BPMNReader;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

public class XMLReader {
    private static int PRETTY_PRINT_INDENT_FACTOR = 4;
    public static String read(String input) {
        String jsonPrettyPrintString = "";
        try {
            JSONObject xmlJSONObj = XML.toJSONObject(input);
            jsonPrettyPrintString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
            System.out.println(jsonPrettyPrintString);
        } catch (JSONException je) {
            System.out.println(je.toString());
        }
        return jsonPrettyPrintString;
    }
}
