package de.dhbw.woped.process2text;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main entry point for the Process-to-Text (P2T) Spring Boot application.
 *
 * <p>The P2T service converts business process models (PNML/BPMN) into
 * human-readable natural language text descriptions. It supports multiple
 * text generation approaches:
 *
 * <ul>
 *   <li><b>Algorithmic generation:</b> Rule-based text generation using NLP techniques</li>
 *   <li><b>LLM-based generation:</b> Using OpenAI GPT, Google Gemini, or local LM Studio</li>
 *   <li><b>RAG-enhanced generation:</b> LLM generation with domain-specific context retrieval</li>
 * </ul>
 *
 * <h2>API Endpoints</h2>
 * <ul>
 *   <li>{@code POST /generateText} - Algorithmic text generation from PNML/BPMN</li>
 *   <li>{@code POST /generateTextLLM} - LLM-based text generation</li>
 *   <li>{@code GET /gptModels} - List available models for a provider</li>
 *   <li>{@code GET /providers} - List supported LLM providers</li>
 * </ul>
 *
 * <h2>Configuration</h2>
 * <p>Service URLs and API endpoints are configured via environment variables
 * or application.properties. See {@link de.dhbw.woped.process2text.config.ServiceConfig}
 * for configuration options.
 *
 * @author WoPeD Team
 * @see de.dhbw.woped.process2text.controller.P2TController
 * @see de.dhbw.woped.process2text.config.ServiceConfig
 */
@SpringBootApplication
@ComponentScan(basePackages = "de.dhbw.woped.process2text")
public class Process2TextApplication {

    /**
     * Application entry point.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(Process2TextApplication.class, args);
    }
}
