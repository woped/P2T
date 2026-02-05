package de.dhbw.woped.process2text.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.dhbw.woped.process2text.service.llm.LlmException;
import de.dhbw.woped.process2text.service.llm.LlmProvider;
import de.dhbw.woped.process2text.service.llm.LlmProviderFactory;
import de.dhbw.woped.process2text.service.llm.dto.LlmRequest;
import de.dhbw.woped.process2text.service.llm.dto.LlmResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
@DisplayName("P2TLLMService Tests")
class P2TLLMServiceTest {

    @Mock
    private LlmProviderFactory providerFactory;

    @Mock
    private TransformerService transformerService;

    @Mock
    private LlmProvider mockProvider;

    private P2TLLMService service;

    @BeforeEach
    void setUp() {
        service = new P2TLLMService(providerFactory, transformerService);
    }

    @Nested
    @DisplayName("generateText")
    class GenerateText {

        @Test
        @DisplayName("should generate text with BPMN input")
        void shouldGenerateTextWithBpmn() {
            String bpmn = "<definitions>...</definitions>";
            String expectedText = "Process description";

            when(transformerService.checkForBPMNorPNML(bpmn)).thenReturn("BPMN");
            when(providerFactory.getProvider("openai")).thenReturn(mockProvider);
            when(mockProvider.generateText(any())).thenReturn(
                    LlmResponse.builder().content(expectedText).build()
            );

            String result = service.generateText(bpmn, "openai", "key", "gpt-4", "Describe");

            assertEquals(expectedText, result);
            verify(transformerService, never()).transform(any(), any());
        }

        @Test
        @DisplayName("should transform PNML to BPMN before processing")
        void shouldTransformPnmlToBpmn() {
            String pnml = "<pnml>...</pnml>";
            String bpmn = "<definitions>...</definitions>";

            when(transformerService.checkForBPMNorPNML(pnml)).thenReturn("PNML");
            when(transformerService.transform("pnmltobpmn", pnml)).thenReturn(bpmn);
            when(providerFactory.getProvider("openai")).thenReturn(mockProvider);
            when(mockProvider.generateText(any())).thenReturn(
                    LlmResponse.builder().content("text").build()
            );

            service.generateText(pnml, "openai", "key", "gpt-4", "Describe");

            verify(transformerService).transform("pnmltobpmn", pnml);
        }

        @Test
        @DisplayName("should build correct LLM request")
        void shouldBuildCorrectRequest() {
            String content = "<definitions>...</definitions>";
            ArgumentCaptor<LlmRequest> requestCaptor = ArgumentCaptor.forClass(LlmRequest.class);

            when(transformerService.checkForBPMNorPNML(content)).thenReturn("BPMN");
            when(providerFactory.getProvider("openai")).thenReturn(mockProvider);
            when(mockProvider.generateText(requestCaptor.capture())).thenReturn(
                    LlmResponse.builder().content("text").build()
            );

            service.generateText(content, "openai", "my-key", "gpt-4", "My prompt");

            LlmRequest captured = requestCaptor.getValue();
            assertEquals("my-key", captured.getApiKey());
            assertEquals("gpt-4", captured.getModel());
            assertEquals("My prompt", captured.getUserPrompt());
            assertEquals(content, captured.getContent());
        }

        @Test
        @DisplayName("should convert LlmException to ResponseStatusException")
        void shouldConvertLlmException() {
            when(transformerService.checkForBPMNorPNML(any())).thenReturn("BPMN");
            when(providerFactory.getProvider("openai")).thenReturn(mockProvider);
            when(mockProvider.generateText(any())).thenThrow(
                    new LlmException(LlmException.ErrorType.AUTHENTICATION_FAILED, "openai", "Invalid key")
            );

            assertThrows(ResponseStatusException.class,
                    () -> service.generateText("<bpmn/>", "openai", "bad-key", "gpt-4", "Test"));
        }
    }

    @Nested
    @DisplayName("getAvailableModels")
    class GetAvailableModels {

        @Test
        @DisplayName("should delegate to provider")
        void shouldDelegateToProvider() {
            List<String> expectedModels = List.of("gpt-4", "gpt-3.5-turbo");

            when(providerFactory.getProvider("openai")).thenReturn(mockProvider);
            when(mockProvider.getAvailableModels("api-key")).thenReturn(expectedModels);

            List<String> result = service.getAvailableModels("openai", "api-key");

            assertEquals(expectedModels, result);
        }
    }

    @Nested
    @DisplayName("getSupportedProviders")
    class GetSupportedProviders {

        @Test
        @DisplayName("should return all provider names")
        void shouldReturnAllProviders() {
            when(providerFactory.getAvailableProviderNames())
                    .thenReturn(List.of("openai", "gemini", "lmstudio"));

            List<String> result = service.getSupportedProviders();

            assertEquals(3, result.size());
            assertTrue(result.contains("openai"));
            assertTrue(result.contains("gemini"));
            assertTrue(result.contains("lmstudio"));
        }
    }

    @Nested
    @DisplayName("providerRequiresApiKey")
    class ProviderRequiresApiKey {

        @Test
        @DisplayName("should return true for OpenAI")
        void shouldReturnTrueForOpenAi() {
            when(providerFactory.getProvider("openai")).thenReturn(mockProvider);
            when(mockProvider.requiresApiKey()).thenReturn(true);

            assertTrue(service.providerRequiresApiKey("openai"));
        }

        @Test
        @DisplayName("should return false for LM Studio")
        void shouldReturnFalseForLmStudio() {
            when(providerFactory.getProvider("lmstudio")).thenReturn(mockProvider);
            when(mockProvider.requiresApiKey()).thenReturn(false);

            assertFalse(service.providerRequiresApiKey("lmstudio"));
        }

        @Test
        @DisplayName("should return true for unknown provider")
        void shouldReturnTrueForUnknown() {
            when(providerFactory.getProvider("unknown"))
                    .thenThrow(new LlmException(LlmException.ErrorType.INVALID_REQUEST, "unknown", "Not found"));

            assertTrue(service.providerRequiresApiKey("unknown"));
        }
    }
}
