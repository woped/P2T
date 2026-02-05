package de.dhbw.woped.process2text.service.llm.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("LlmRequest Tests")
class LlmRequestTest {

    @Nested
    @DisplayName("Builder")
    class Builder {

        @Test
        @DisplayName("should create request with all fields")
        void shouldCreateWithAllFields() {
            LlmRequest request = LlmRequest.builder()
                    .apiKey("test-key")
                    .model("gpt-4")
                    .systemPrompt("You are helpful")
                    .userPrompt("Describe this")
                    .content("<bpmn>...</bpmn>")
                    .temperature(0.5)
                    .maxTokens(2000)
                    .build();

            assertEquals("test-key", request.getApiKey());
            assertEquals("gpt-4", request.getModel());
            assertEquals("You are helpful", request.getSystemPrompt());
            assertEquals("Describe this", request.getUserPrompt());
            assertEquals("<bpmn>...</bpmn>", request.getContent());
            assertEquals(0.5, request.getTemperature());
            assertEquals(2000, request.getMaxTokens());
        }

        @Test
        @DisplayName("should use default values")
        void shouldUseDefaults() {
            LlmRequest request = LlmRequest.builder()
                    .model("gpt-4")
                    .userPrompt("Test")
                    .build();

            assertEquals("You are a helpful assistant.", request.getSystemPrompt());
            assertEquals("", request.getContent());
            assertEquals(0.7, request.getTemperature());
            assertEquals(4096, request.getMaxTokens());
        }

        @Test
        @DisplayName("should throw on missing model")
        void shouldThrowOnMissingModel() {
            assertThrows(NullPointerException.class, () ->
                    LlmRequest.builder()
                            .userPrompt("Test")
                            .build()
            );
        }

        @Test
        @DisplayName("should throw on missing userPrompt")
        void shouldThrowOnMissingUserPrompt() {
            assertThrows(NullPointerException.class, () ->
                    LlmRequest.builder()
                            .model("gpt-4")
                            .build()
            );
        }
    }

    @Nested
    @DisplayName("getCombinedUserMessage")
    class GetCombinedUserMessage {

        @Test
        @DisplayName("should combine prompt and content")
        void shouldCombinePromptAndContent() {
            LlmRequest request = LlmRequest.builder()
                    .model("gpt-4")
                    .userPrompt("Describe this process")
                    .content("<bpmn>xml</bpmn>")
                    .build();

            String combined = request.getCombinedUserMessage();

            assertTrue(combined.contains("Describe this process"));
            assertTrue(combined.contains("<bpmn>xml</bpmn>"));
        }

        @Test
        @DisplayName("should return only prompt when no content")
        void shouldReturnOnlyPrompt() {
            LlmRequest request = LlmRequest.builder()
                    .model("gpt-4")
                    .userPrompt("Describe this process")
                    .build();

            assertEquals("Describe this process", request.getCombinedUserMessage());
        }
    }
}
