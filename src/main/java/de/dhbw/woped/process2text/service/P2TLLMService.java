package de.dhbw.woped.process2text.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

@Service
public class P2TLLMService {

    public String callLLM(String text, String apiKey) {
        String apiUrl = "https://api.openai.com/v1/engines/davinci-codex/completions";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("prompt", text);
        requestBody.put("max_tokens", 150);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        return restTemplate.postForObject(apiUrl, entity, String.class);
    }

    public String callTransformer(String file) {

    }

    /* the method analyzes a given XML content in the form of a string and determines the file format based
    on the root element of the XML document */
    public String checkForBPMNorPNML(String file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(new ByteArrayInputStream(file.getBytes()));
        document.getDocumentElement().normalize();

        Element rootElement = document.getDocumentElement();
        String rootTag = rootElement.getTagName();

        if ("pnml".equalsIgnoreCase(rootTag)) {
            return "PNML";
        } else if ("definitions".equalsIgnoreCase(rootTag)) {
            return "BPMN";
        } else {
            return "Unknown";
        }
    }
}
