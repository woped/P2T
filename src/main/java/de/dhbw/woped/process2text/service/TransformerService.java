package de.dhbw.woped.process2text.service;

import de.dhbw.woped.process2text.config.ServiceConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Service for transforming process models between PNML and BPMN formats.
 *
 * <p>This service acts as a client to the external Model Transformer microservice,
 * which handles the actual conversion logic. The transformer service URL is configured
 * via {@link ServiceConfig}.
 *
 * <h2>Supported Transformations</h2>
 * <ul>
 *   <li>PNML to BPMN ({@code direction=pnmltobpmn})</li>
 *   <li>BPMN to PNML ({@code direction=bpmntopnml})</li>
 * </ul>
 *
 * <h2>Format Detection</h2>
 * <p>The service can automatically detect whether an input file is PNML or BPMN
 * based on the XML root element.
 *
 * <h2>Error Handling</h2>
 * <p>If transformation fails (e.g., service unavailable), the original input
 * is returned unchanged to allow graceful degradation.
 *
 * @author WoPeD Team
 * @see ServiceConfig#getTransformerServiceUrl()
 */
@Service
public class TransformerService {

    private static final Logger logger = LoggerFactory.getLogger(TransformerService.class);

    private final WebClient webClient;

    /**
     * Creates a new TransformerService with the configured transformer URL.
     *
     * @param serviceConfig the service configuration containing the transformer URL
     */
    public TransformerService(ServiceConfig serviceConfig) {
        String transformerUrl = serviceConfig.getTransformerServiceUrl();
        logger.info("Initializing TransformerService with URL: {}", transformerUrl);
        this.webClient = WebClient.builder().baseUrl(transformerUrl).build();
    }

    /**
     * Transforms a process model from one format to another.
     *
     * <p>Calls the external transformer API to convert the provided XML content.
     * If the transformation fails for any reason, the original content is returned.
     *
     * @param direction the transformation direction (e.g., "pnmltobpmn" or "bpmntopnml")
     * @param xmlContent the XML content to transform (PNML or BPMN)
     * @return the transformed content, or the original content if transformation fails
     */
    public String transform(String direction, String xmlContent) {
        try {
            // Validate XML format
            Document document = Jsoup.parse(xmlContent, "", Parser.xmlParser());
            logger.info("XML format is valid. Root element: {}", document.child(0).tagName());

            String endpoint =
                UriComponentsBuilder.fromUriString("/transform")
                    .queryParam("direction", direction)
                    .toUriString();

            logger.info("Sending request to transformer service: {}", endpoint);

            // Send the request as XML
            String result =
                this.webClient
                    .post()
                    .uri(endpoint)
                    .contentType(MediaType.APPLICATION_XML)
                    .bodyValue(xmlContent)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (result == null || result.trim().isEmpty()) {
                logger.warn("Transformer service returned empty response");
                return xmlContent;
            }

            logger.info("Transformation successful. Result length: {}", result.length());
            return result;

        } catch (Exception e) {
            logger.error("Transformation error: {}", e.getMessage());
            // On error, return the original content for graceful degradation
            return xmlContent;
        }
    }

    /**
     * Detects the format of a process model XML file.
     *
     * <p>Examines the root element of the XML to determine whether it's a PNML
     * or BPMN document.
     *
     * @param xmlContent the XML content to analyze
     * @return "PNML" if the root element is {@code <pnml>},
     *         "BPMN" if the root element is {@code <definitions>},
     *         or "Unknown" otherwise
     */
    public String checkForBPMNorPNML(String xmlContent) {
        Document document = Jsoup.parse(xmlContent, "", Parser.xmlParser());

        Element rootElement = document.child(0);
        String rootTag = rootElement.tagName();

        if ("pnml".equalsIgnoreCase(rootTag)) {
            return "PNML";
        } else if ("definitions".equalsIgnoreCase(rootTag)) {
            return "BPMN";
        } else {
            return "Unknown";
        }
    }
}
