package de.dhbw.woped.process2text.service;

import de.dhbw.woped.process2text.config.ServiceConfig;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Service for Retrieval-Augmented Generation (RAG) operations.
 *
 * <p>RAG enhances LLM prompts by retrieving relevant context from a knowledge base
 * before generating responses. This service acts as a client to the external RAG
 * microservice.
 *
 * <h2>Features</h2>
 * <ul>
 *   <li><b>Prompt Enrichment:</b> Augments user prompts with domain-specific context</li>
 *   <li><b>Semantic Search:</b> Finds relevant documents in the knowledge base</li>
 *   <li><b>Health Check:</b> Verifies RAG service availability</li>
 * </ul>
 *
 * <h2>Error Handling</h2>
 * <p>All methods implement graceful degradation. If the RAG service is unavailable
 * or returns an error, the original input is returned unchanged to allow the
 * application to continue functioning.
 *
 * <h2>Configuration</h2>
 * <p>The RAG service URL is configured via {@link ServiceConfig#getRagServiceUrl()}.
 *
 * @author WoPeD Team
 * @see ServiceConfig
 * @see de.dhbw.woped.process2text.controller.P2TController#generateTextLLM
 */
@Service
public class RagService {

    private static final Logger logger = LoggerFactory.getLogger(RagService.class);

    private final ServiceConfig serviceConfig;
    private final RestTemplate restTemplate;

    /**
     * Creates a new RagService with the given configuration.
     *
     * @param serviceConfig the service configuration containing the RAG service URL
     */
    public RagService(ServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Enriches a prompt with relevant context from the RAG knowledge base.
     *
     * <p>Sends the original prompt and the process diagram to the RAG service,
     * which retrieves relevant context and constructs an enhanced prompt.
     *
     * <p>If the RAG service is unavailable or returns an error, the original
     * prompt is returned unchanged.
     *
     * @param originalPrompt the original user prompt
     * @param diagram        the process model content (PNML or BPMN XML)
     * @return the enriched prompt with additional context, or the original prompt if enrichment fails
     */
    public String enrichPrompt(String originalPrompt, String diagram) {
        String ragUrl = serviceConfig.getRagServiceUrl();
        logger.info("Calling RAG service at {} to enrich prompt", ragUrl);

        try {
            JSONObject requestJson = new JSONObject();
            requestJson.put("prompt", originalPrompt);
            requestJson.put("diagram", diagram);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(requestJson.toString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    ragUrl + "/rag/enrich", entity, String.class);

            if (response.getBody() == null) {
                logger.warn("RAG service returned empty response");
                return originalPrompt;
            }

            JSONObject responseJson = new JSONObject(response.getBody());
            String enrichedPrompt = responseJson.getString("enriched_prompt");

            logger.info("RAG enrichment successful. Original: {} chars, Enriched: {} chars",
                    originalPrompt.length(), enrichedPrompt.length());

            return enrichedPrompt;

        } catch (RestClientException e) {
            logger.warn("RAG service unavailable, using original prompt: {}", e.getMessage());
            return originalPrompt;
        } catch (Exception e) {
            logger.error("RAG enrichment failed, using original prompt", e);
            return originalPrompt;
        }
    }

    /**
     * Performs a semantic search in the RAG knowledge base.
     *
     * <p>Searches for documents relevant to the given query using vector
     * similarity search.
     *
     * @param query the search query text
     * @param limit maximum number of results to return
     * @return search results as a JSON string, or null if the search fails
     */
    public String search(String query, int limit) {
        String ragUrl = serviceConfig.getRagServiceUrl();

        try {
            String url = String.format("%s/rag/search?query=%s&limit=%d", ragUrl, query, limit);
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            logger.error("RAG search failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Checks if the RAG service is currently available.
     *
     * <p>Performs a lightweight health check by calling the debug endpoint.
     *
     * @return {@code true} if the RAG service is reachable and responding,
     *         {@code false} otherwise
     */
    public boolean isAvailable() {
        String ragUrl = serviceConfig.getRagServiceUrl();

        try {
            restTemplate.getForObject(ragUrl + "/rag/debug_dump", String.class);
            return true;
        } catch (Exception e) {
            logger.debug("RAG service not available: {}", e.getMessage());
            return false;
        }
    }
}
