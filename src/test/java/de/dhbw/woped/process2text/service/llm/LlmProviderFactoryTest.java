package de.dhbw.woped.process2text.service.llm;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.dhbw.woped.process2text.service.llm.dto.LlmRequest;
import de.dhbw.woped.process2text.service.llm.dto.LlmResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("LlmProviderFactory Tests")
class LlmProviderFactoryTest {

    private LlmProvider mockOpenAiProvider;
    private LlmProvider mockGeminiProvider;
    private LlmProviderFactory factory;

    @BeforeEach
    void setUp() {
        mockOpenAiProvider = mock(LlmProvider.class);
        mockGeminiProvider = mock(LlmProvider.class);

        when(mockOpenAiProvider.getProviderName()).thenReturn("openai");
        when(mockGeminiProvider.getProviderName()).thenReturn("gemini");

        factory = new LlmProviderFactory(List.of(mockOpenAiProvider, mockGeminiProvider));
    }

    @Nested
    @DisplayName("getProvider")
    class GetProvider {

        @Test
        @DisplayName("should return correct provider for valid name")
        void shouldReturnProviderForValidName() {
            LlmProvider provider = factory.getProvider("openai");
            assertEquals(mockOpenAiProvider, provider);
        }

        @Test
        @DisplayName("should be case-insensitive")
        void shouldBeCaseInsensitive() {
            assertEquals(mockOpenAiProvider, factory.getProvider("OPENAI"));
            assertEquals(mockOpenAiProvider, factory.getProvider("OpenAI"));
            assertEquals(mockGeminiProvider, factory.getProvider("GEMINI"));
        }

        @Test
        @DisplayName("should throw exception for unknown provider")
        void shouldThrowForUnknownProvider() {
            LlmException exception = assertThrows(LlmException.class,
                    () -> factory.getProvider("unknown"));

            assertEquals(LlmException.ErrorType.INVALID_REQUEST, exception.getErrorType());
            assertTrue(exception.getMessage().contains("unknown"));
        }

        @Test
        @DisplayName("should throw exception for null provider")
        void shouldThrowForNullProvider() {
            assertThrows(LlmException.class, () -> factory.getProvider(null));
        }

        @Test
        @DisplayName("should throw exception for empty provider")
        void shouldThrowForEmptyProvider() {
            assertThrows(LlmException.class, () -> factory.getProvider(""));
            assertThrows(LlmException.class, () -> factory.getProvider("  "));
        }
    }

    @Nested
    @DisplayName("hasProvider")
    class HasProvider {

        @Test
        @DisplayName("should return true for existing provider")
        void shouldReturnTrueForExisting() {
            assertTrue(factory.hasProvider("openai"));
            assertTrue(factory.hasProvider("gemini"));
        }

        @Test
        @DisplayName("should return false for non-existing provider")
        void shouldReturnFalseForNonExisting() {
            assertFalse(factory.hasProvider("unknown"));
        }

        @Test
        @DisplayName("should return false for null")
        void shouldReturnFalseForNull() {
            assertFalse(factory.hasProvider(null));
        }
    }

    @Nested
    @DisplayName("getAvailableProviderNames")
    class GetAvailableProviderNames {

        @Test
        @DisplayName("should return all registered provider names")
        void shouldReturnAllNames() {
            List<String> names = factory.getAvailableProviderNames();

            assertEquals(2, names.size());
            assertTrue(names.contains("openai"));
            assertTrue(names.contains("gemini"));
        }
    }
}
