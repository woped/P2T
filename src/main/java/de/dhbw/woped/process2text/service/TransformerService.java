package de.dhbw.woped.process2text.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class TransformerService {

    private final WebClient webClient;

    @Autowired
    public TransformerService(WebClient.Builder webClientBuilder){
        this.webClient = webClientBuilder.baseUrl("\"https://europe-west3-woped-422510.cloudfunctions.net\"").build();
    }



    public String callTransformer(String direction, String file) {
        String endpoint = UriComponentsBuilder.fromUriString("/transform").queryParam("direction", direction)
                .build().toUriString();
        return this.webClient.post()
                .uri(endpoint)
                .body(BodyInserters.fromFormData("bpmn", file))
                .retrieve()
                .bodyToMono(String.class)
                .block();
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
