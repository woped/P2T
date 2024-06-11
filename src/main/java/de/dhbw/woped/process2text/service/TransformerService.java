package de.dhbw.woped.process2text.service;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class TransformerService {

  private final WebClient webClient;

  public TransformerService() {
    this.webClient =
        WebClient.builder().baseUrl("https://europe-west3-woped-422510.cloudfunctions.net").build();
  }

  public String transform(String direction, String pnmlXml) {
    String endpoint =
        UriComponentsBuilder.fromUriString("/transform")
            .queryParam("direction", direction)
            .toUriString();

    return this.webClient
        .post()
        .uri(endpoint)
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromMultipartData("pnml", pnmlXml))
        .retrieve()
        .bodyToMono(String.class)
        .block();
  }

  /* the method analyzes a given XML content in the form of a string and determines the file format based
  on the root element of the HTML document */
  public String checkForBPMNorPNML(String file) throws IOException {
    // Parse the HTML content
    Document document = Jsoup.parse(file);

    // Get the root element
    Element rootElement = document.child(0);
    String rootTag = rootElement.tagName();

    // Check the root element tag name
    if ("pnml".equalsIgnoreCase(rootTag)) {
      return "PNML";
    } else if ("definitions".equalsIgnoreCase(rootTag)) {
      return "BPMN";
    } else {
      return "Unknown";
    }
  }
}
