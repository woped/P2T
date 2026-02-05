package de.dhbw.woped.process2text.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.dhbw.woped.process2text.service.P2TLLMService;
import de.dhbw.woped.process2text.service.P2TService;
import de.dhbw.woped.process2text.service.RagService;
import de.dhbw.woped.process2text.service.llm.LlmException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Unit tests for P2TController.
 * Tests controller logic in isolation without Spring context.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("P2TController Tests")
class P2TControllerTest {

    @Mock
    private P2TService p2tService;

    @Mock
    private P2TLLMService llmService;

    @Mock
    private RagService ragService;

    @Mock
    private Counter httpRequestsTotal;

    @Mock
    private Timer httpRequestDuration;

    private P2TController controller;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        // Mock the Timer.record() method to execute the supplier and return its result
        when(httpRequestDuration.record(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });
        controller = new P2TController(p2tService, llmService, ragService, httpRequestsTotal, httpRequestDuration);
    }

    @Nested
    @DisplayName("generateText")
    class GenerateText {

        @Test
        @DisplayName("should generate text from PNML")
        void shouldGenerateTextFromPnml() {
            String pnml = "<pnml><net>test</net></pnml>";
            String expectedText = "This process describes a test workflow.";

            when(p2tService.generateText(pnml)).thenReturn(expectedText);

            String result = controller.generateText(pnml);

            assertEquals(expectedText, result);
            verify(p2tService).generateText(pnml);
            verify(httpRequestsTotal).increment();
        }

        @Test
        @DisplayName("should handle empty input")
        void shouldHandleEmptyInput() {
            when(p2tService.generateText("")).thenReturn("");

            String result = controller.generateText("");

            assertEquals("", result);
            verify(httpRequestsTotal).increment();
        }
    }

    @Nested
    @DisplayName("generateTextLLM")
    class GenerateTextLLM {

        @Test
        @DisplayName("should generate text using OpenAI")
        void shouldGenerateTextUsingOpenAi() {
            String bpmn = "<definitions>...</definitions>";
            String expectedText = "LLM generated description";

            when(llmService.getSupportedProviders()).thenReturn(List.of("openai", "gemini", "lmstudio"));
            when(llmService.providerRequiresApiKey("openai")).thenReturn(true);
            when(llmService.generateText(eq(bpmn), eq("openai"), eq("test-key"), eq("gpt-4"), anyString()))
                    .thenReturn(expectedText);

            String result = controller.generateTextLLM(
                    bpmn, "test-key", "Describe this process", "gpt-4", "openai", false);

            assertEquals(expectedText, result);
        }

        @Test
        @DisplayName("should use RAG when enabled")
        void shouldUseRagWhenEnabled() {
            String bpmn = "<definitions>...</definitions>";
            String originalPrompt = "Describe this";
            String enrichedPrompt = "Describe this with context";
            String expectedText = "LLM response";

            when(llmService.getSupportedProviders()).thenReturn(List.of("openai"));
            when(llmService.providerRequiresApiKey("openai")).thenReturn(true);
            when(ragService.enrichPrompt(originalPrompt, bpmn)).thenReturn(enrichedPrompt);
            when(llmService.generateText(eq(bpmn), eq("openai"), eq("key"), eq("gpt-4"), eq(enrichedPrompt)))
                    .thenReturn(expectedText);

            String result = controller.generateTextLLM(
                    bpmn, "key", originalPrompt, "gpt-4", "openai", true);

            assertEquals(expectedText, result);
            verify(ragService).enrichPrompt(originalPrompt, bpmn);
        }

        @Test
        @DisplayName("should reject missing API key for OpenAI")
        void shouldRejectMissingApiKey() {
            when(llmService.getSupportedProviders()).thenReturn(List.of("openai"));
            when(llmService.providerRequiresApiKey("openai")).thenReturn(true);

            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                    controller.generateTextLLM(
                            "<bpmn>test</bpmn>", null, "Test", "gpt-4", "openai", false));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertTrue(exception.getReason().contains("API key"));
        }

        @Test
        @DisplayName("should allow LM Studio without API key")
        void shouldAllowLmStudioWithoutApiKey() {
            when(llmService.getSupportedProviders()).thenReturn(List.of("lmstudio"));
            when(llmService.providerRequiresApiKey("lmstudio")).thenReturn(false);
            when(llmService.generateText(any(), eq("lmstudio"), isNull(), any(), any()))
                    .thenReturn("response");

            String result = controller.generateTextLLM(
                    "<bpmn>test</bpmn>", null, "Test", "local-model", "lmstudio", false);

            assertEquals("response", result);
        }

        @Test
        @DisplayName("should reject invalid provider")
        void shouldRejectInvalidProvider() {
            when(llmService.getSupportedProviders()).thenReturn(List.of("openai"));

            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                    controller.generateTextLLM(
                            "<bpmn>test</bpmn>", "key", "Test", "model", "invalid", false));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertTrue(exception.getReason().contains("provider"));
        }

        @Test
        @DisplayName("should propagate LLM exceptions")
        void shouldPropagateLlmExceptions() {
            when(llmService.getSupportedProviders()).thenReturn(List.of("openai"));
            when(llmService.providerRequiresApiKey("openai")).thenReturn(true);
            when(llmService.generateText(any(), eq("openai"), eq("key"), any(), any()))
                    .thenThrow(new LlmException(LlmException.ErrorType.UNKNOWN_ERROR, "openai", "API Error"));

            assertThrows(LlmException.class, () ->
                    controller.generateTextLLM(
                            "<bpmn>test</bpmn>", "key", "Test", "gpt-4", "openai", false));
        }
    }

    @Nested
    @DisplayName("getModels")
    class GetModels {

        @Test
        @DisplayName("should return OpenAI models")
        void shouldReturnOpenAiModels() {
            List<String> models = List.of("gpt-4", "gpt-3.5-turbo");
            when(llmService.getAvailableModels("openai", "test-key")).thenReturn(models);

            // Note: getModels takes (apiKey, provider) not (provider, apiKey)
            List<String> result = controller.getModels("test-key", "openai");

            assertEquals(models, result);
        }

        @Test
        @DisplayName("should return LM Studio models without API key")
        void shouldReturnLmStudioModels() {
            List<String> models = List.of("llama-3.2", "mistral-7b");
            when(llmService.getAvailableModels("lmstudio", null)).thenReturn(models);

            // Note: getModels takes (apiKey, provider)
            List<String> result = controller.getModels(null, "lmstudio");

            assertEquals(models, result);
        }

        @Test
        @DisplayName("should reject invalid provider")
        void shouldRejectInvalidProvider() {
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                    controller.getModels(null, "invalid"));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        }
    }

    @Nested
    @DisplayName("getProviders")
    class GetProviders {

        @Test
        @DisplayName("should return supported providers")
        void shouldReturnProviders() {
            List<String> providers = List.of("openai", "gemini", "lmstudio");
            when(llmService.getSupportedProviders()).thenReturn(providers);

            List<String> result = controller.getProviders();

            assertEquals(providers, result);
        }
    }
}
