package de.dhbw.woped.process2text.service.llm;

import de.dhbw.woped.process2text.service.llm.LlmException.ErrorType;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Factory component for managing and retrieving LLM provider instances.
 *
 * <p>This factory uses Spring's dependency injection to automatically collect
 * all beans implementing the {@link LlmProvider} interface. It provides a
 * centralized registry for provider lookup by name.
 *
 * <h2>How It Works</h2>
 * <ol>
 *   <li>On startup, Spring injects all {@link LlmProvider} beans into the constructor</li>
 *   <li>Providers are indexed by their lowercase name for case-insensitive lookup</li>
 *   <li>Clients request providers by name using {@link #getProvider(String)}</li>
 * </ol>
 *
 * <h2>Adding New Providers</h2>
 * <p>To add a new LLM provider:
 * <ol>
 *   <li>Create a class implementing {@link LlmProvider}</li>
 *   <li>Annotate it with {@code @Component} or {@code @Service}</li>
 *   <li>The factory will automatically discover and register it</li>
 * </ol>
 *
 * <h2>Thread Safety</h2>
 * <p>This factory is thread-safe. The internal provider map is immutable after construction.
 *
 * @author WoPeD Team
 * @see LlmProvider
 */
@Component
public class LlmProviderFactory {

    private static final Logger logger = LoggerFactory.getLogger(LlmProviderFactory.class);

    /** Immutable map of provider name to provider instance */
    private final Map<String, LlmProvider> providers;

    /**
     * Constructs the factory and registers all available LLM providers.
     *
     * <p>Spring automatically injects all beans implementing {@link LlmProvider}
     * into this constructor.
     *
     * @param providerList list of all available LLM provider implementations
     */
    public LlmProviderFactory(List<LlmProvider> providerList) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(
                        provider -> provider.getProviderName().toLowerCase(),
                        Function.identity()
                ));

        logger.info("Initialized LlmProviderFactory with {} providers: {}",
                providers.size(), providers.keySet());
    }

    /**
     * Retrieves a provider instance by name.
     *
     * <p>Provider names are matched case-insensitively (e.g., "OpenAI" and "openai"
     * both work).
     *
     * @param providerName the provider name (case-insensitive)
     * @return the provider instance
     * @throws LlmException with {@link ErrorType#INVALID_REQUEST} if the provider
     *         name is empty or the provider is not found
     */
    public LlmProvider getProvider(String providerName) {
        if (providerName == null || providerName.trim().isEmpty()) {
            throw new LlmException(ErrorType.INVALID_REQUEST, "unknown",
                    "Provider name must not be empty");
        }

        String normalizedName = providerName.toLowerCase().trim();
        LlmProvider provider = providers.get(normalizedName);

        if (provider == null) {
            throw new LlmException(ErrorType.INVALID_REQUEST, normalizedName,
                    "Unknown LLM provider: " + providerName + ". Available: " + getAvailableProviderNames());
        }

        return provider;
    }

    /**
     * Checks if a provider with the given name is registered.
     *
     * @param providerName the provider name to check (case-insensitive)
     * @return {@code true} if the provider exists, {@code false} otherwise
     */
    public boolean hasProvider(String providerName) {
        return providerName != null && providers.containsKey(providerName.toLowerCase().trim());
    }

    /**
     * Returns the list of all available provider names.
     *
     * @return unmodifiable list of provider names
     */
    public List<String> getAvailableProviderNames() {
        return List.copyOf(providers.keySet());
    }

    /**
     * Returns all registered providers as a map.
     *
     * @return unmodifiable map of provider name to provider instance
     */
    public Map<String, LlmProvider> getAllProviders() {
        return Map.copyOf(providers);
    }
}
