package de.dhbw.woped.process2text.service.llm.dto;

/**
 * Immutable Data Transfer Object (DTO) for LLM API responses.
 *
 * <p>This class encapsulates the response from an LLM provider, including
 * the generated text content and usage metadata. It uses the Builder pattern
 * for flexible construction while ensuring immutability after creation.
 *
 * <h2>Response Fields</h2>
 * <ul>
 *   <li>{@code content} - The generated text response</li>
 *   <li>{@code model} - The model that generated the response</li>
 *   <li>{@code provider} - The LLM provider name</li>
 *   <li>{@code promptTokens} - Number of tokens in the input</li>
 *   <li>{@code completionTokens} - Number of tokens in the output</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * LlmResponse response = LlmResponse.builder()
 *     .content("The process starts with...")
 *     .model("gpt-4")
 *     .provider("openai")
 *     .promptTokens(150)
 *     .completionTokens(200)
 *     .build();
 * }</pre>
 *
 * @author WoPeD Team
 * @see LlmRequest
 * @see de.dhbw.woped.process2text.service.llm.LlmProvider#generateText(LlmRequest)
 */
public final class LlmResponse {

    private final String content;
    private final String model;
    private final String provider;
    private final int promptTokens;
    private final int completionTokens;

    /**
     * Private constructor used by the Builder.
     *
     * @param builder the builder instance
     */
    private LlmResponse(Builder builder) {
        this.content = builder.content;
        this.model = builder.model;
        this.provider = builder.provider;
        this.promptTokens = builder.promptTokens;
        this.completionTokens = builder.completionTokens;
    }

    /**
     * Returns the generated text content.
     *
     * @return the generated text
     */
    public String getContent() {
        return content;
    }

    /**
     * Returns the model that generated this response.
     *
     * @return the model identifier
     */
    public String getModel() {
        return model;
    }

    /**
     * Returns the provider that generated this response.
     *
     * @return the provider name (e.g., "openai", "gemini")
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Returns the number of tokens in the input prompt.
     *
     * @return prompt token count
     */
    public int getPromptTokens() {
        return promptTokens;
    }

    /**
     * Returns the number of tokens in the generated response.
     *
     * @return completion token count
     */
    public int getCompletionTokens() {
        return completionTokens;
    }

    /**
     * Returns the total token count (prompt + completion).
     *
     * <p>Useful for tracking API usage and costs.
     *
     * @return total token count
     */
    public int getTotalTokens() {
        return promptTokens + completionTokens;
    }

    /**
     * Creates a new Builder instance.
     *
     * @return a new Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for constructing {@link LlmResponse} instances.
     */
    public static class Builder {
        private String content;
        private String model;
        private String provider;
        private int promptTokens;
        private int completionTokens;

        /**
         * Sets the generated text content.
         *
         * @param content the generated text
         * @return this builder
         */
        public Builder content(String content) {
            this.content = content;
            return this;
        }

        /**
         * Sets the model identifier.
         *
         * @param model the model name
         * @return this builder
         */
        public Builder model(String model) {
            this.model = model;
            return this;
        }

        /**
         * Sets the provider name.
         *
         * @param provider the provider name
         * @return this builder
         */
        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        /**
         * Sets the prompt token count.
         *
         * @param promptTokens number of tokens in the input
         * @return this builder
         */
        public Builder promptTokens(int promptTokens) {
            this.promptTokens = promptTokens;
            return this;
        }

        /**
         * Sets the completion token count.
         *
         * @param completionTokens number of tokens in the output
         * @return this builder
         */
        public Builder completionTokens(int completionTokens) {
            this.completionTokens = completionTokens;
            return this;
        }

        /**
         * Builds the immutable LlmResponse.
         *
         * @return a new LlmResponse instance
         */
        public LlmResponse build() {
            return new LlmResponse(this);
        }
    }

    /**
     * Returns a string representation for logging.
     *
     * @return formatted string with key fields
     */
    @Override
    public String toString() {
        return "LlmResponse{" +
                "provider='" + provider + '\'' +
                ", model='" + model + '\'' +
                ", contentLength=" + (content != null ? content.length() : 0) +
                ", totalTokens=" + getTotalTokens() +
                '}';
    }
}
