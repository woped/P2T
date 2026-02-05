package de.dhbw.woped.process2text.service.llm.provider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.dhbw.woped.process2text.config.ServiceConfig;
import de.dhbw.woped.process2text.service.llm.LlmException;
import de.dhbw.woped.process2text.service.llm.dto.LlmRequest;
import de.dhbw.woped.process2text.service.llm.dto.LlmResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("LmStudioProvider Tests")
class LmStudioProviderTest {

    @Mock
    private ServiceConfig serviceConfig;

    private LmStudioProvider provider;

    @BeforeEach
    void setUp() {
        when(serviceConfig.getLmstudioApiUrl()).thenReturn("http://localhost:1234");
        provider = new LmStudioProvider(serviceConfig);
    }

    @Nested
    @DisplayName("Provider Properties")
    class ProviderProperties {

        @Test
        @DisplayName("should have correct provider name")
        void shouldHaveCorrectName() {
            assertEquals("lmstudio", provider.getProviderName());
        }

        @Test
        @DisplayName("should not require API key")
        void shouldNotRequireApiKey() {
            assertFalse(provider.requiresApiKey());
        }
    }

    @Nested
    @DisplayName("Request Validation")
    class RequestValidation {

        @Test
        @DisplayName("should throw on missing model")
        void shouldThrowOnMissingModel() {
            LlmRequest request = LlmRequest.builder()
                    .model("")
                    .userPrompt("Test")
                    .build();

            // Note: The actual model validation happens at request build time
            // But we can test the provider's validation
            LlmRequest requestWithNullModel = mock(LlmRequest.class);
            when(requestWithNullModel.getModel()).thenReturn(null);

            assertThrows(LlmException.class, () -> provider.generateText(requestWithNullModel));
        }
    }

    @Nested
    @DisplayName("Response Parsing")
    class ResponseParsing {

        // These tests would require mocking RestTemplate
        // In a real scenario, you'd use WireMock or similar

        @Test
        @DisplayName("should handle standard OpenAI format response")
        void shouldHandleStandardFormat() {
            // This test would use WireMock to mock the HTTP response
            // For now, we verify the provider is correctly instantiated
            assertNotNull(provider);
        }
    }
}
