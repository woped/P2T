package de.dhbw.woped.process2text.service.llm.dto;

import java.util.Objects;

/**
 * Immutable Data Transfer Object (DTO) for LLM API requests.
 *
 * <p>This class encapsulates all parameters needed for making requests to
 * LLM providers. It uses the Builder pattern for flexible construction
 * while ensuring immutability after creation.
 *
 * <h2>Required Fields</h2>
 * <ul>
 *   <li>{@code model} - The model identifier (e.g., "gpt-4")</li>
 *   <li>{@code userPrompt} - The user's instruction prompt</li>
 * </ul>
 *
 * <h2>Optional Fields</h2>
 * <ul>
 *   <li>{@code apiKey} - API key for authentication (may be null for local providers)</li>
 *   <li>{@code systemPrompt} - System context (defaults to "You are a helpful assistant.")</li>
 *   <li>{@code content} - Additional content like process model XML</li>
 *   <li>{@code temperature} - Creativity parameter (default: 0.7)</li>
 *   <li>{@code maxTokens} - Maximum response tokens (default: 4096)</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * LlmRequest request = LlmRequest.builder()
 *     .apiKey("sk-...")
 *     .model("gpt-4")
 *     .userPrompt("Describe this process")
 *     .content("<bpmn:definitions>...</bpmn:definitions>")
 *     .build();
 * }</pre>
 *
 * @author WoPeD Team
 * @see LlmResponse
 * @see de.dhbw.woped.process2text.service.llm.LlmProvider#generateText(LlmRequest)
 */
public final class LlmRequest {

    private final String apiKey;
    private final String model;
    private final String systemPrompt;
    private final String userPrompt;
    private final String content;
    private final double temperature;
    private final int maxTokens;

    /**
     * Private constructor used by the Builder.
     *
     * @param builder the builder instance
     * @throws NullPointerException if model or userPrompt is null
     */
    private LlmRequest(Builder builder) {
        this.apiKey = builder.apiKey;
        this.model = Objects.requireNonNull(builder.model, "Model must not be null");
        this.systemPrompt = builder.systemPrompt != null ? builder.systemPrompt : "You are a helpful assistant.";
        this.userPrompt = Objects.requireNonNull(builder.userPrompt, "User prompt must not be null");
        this.content = builder.content != null ? builder.content : "";
        this.temperature = builder.temperature;
        this.maxTokens = builder.maxTokens;
    }

    /**
     * Returns the API key for authentication.
     *
     * @return the API key, may be null for local providers
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Returns the model identifier.
     *
     * @return the model name (e.g., "gpt-4", "gemini-pro")
     */
    public String getModel() {
        return model;
    }

    /**
     * Returns the system prompt that sets the LLM's context.
     *
     * @return the system prompt
     */
    public String getSystemPrompt() {
        return systemPrompt;
    }

    /**
     * Returns the user's instruction prompt.
     *
     * @return the user prompt
     */
    public String getUserPrompt() {
        return userPrompt;
    }

    /**
     * Returns the additional content (e.g., process model XML).
     *
     * @return the content, may be empty but never null
     */
    public String getContent() {
        return content;
    }

    /**
     * Returns the temperature parameter controlling response creativity.
     *
     * @return temperature value (0.0 = deterministic, 1.0+ = creative)
     */
    public double getTemperature() {
        return temperature;
    }

    /**
     * Returns the maximum number of tokens in the response.
     *
     * @return maximum tokens
     */
    public int getMaxTokens() {
        return maxTokens;
    }

    /**
     * Returns the user prompt combined with content.
     *
     * <p>This method concatenates the user prompt and content with a double newline,
     * useful for APIs that expect a single user message.
     *
     * @return combined message: userPrompt + "\n\n" + content (or just userPrompt if content is empty)
     */
    public String getCombinedUserMessage() {
        if (content == null || content.isEmpty()) {
            return userPrompt;
        }
        return userPrompt + "\n\n" + content;
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
     * Builder class for constructing {@link LlmRequest} instances.
     */
    public static class Builder {
        private String apiKey;
        private String model;
        private String systemPrompt;
        private String userPrompt;
        private String content;
        private double temperature = 0.7;
        private int maxTokens = 4096;

        /**
         * Sets the API key for authentication.
         *
         * @param apiKey the API key
         * @return this builder
         */
        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        /**
         * Sets the model identifier (required).
         *
         * @param model the model name (e.g., "gpt-4")
         * @return this builder
         */
        public Builder model(String model) {
            this.model = model;
            return this;
        }

        /**
         * Sets the system prompt for context.
         *
         * @param systemPrompt the system prompt
         * @return this builder
         */
        public Builder systemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }

        /**
         * Sets the user prompt (required).
         *
         * @param userPrompt the user's instruction
         * @return this builder
         */
        public Builder userPrompt(String userPrompt) {
            this.userPrompt = userPrompt;
            return this;
        }

        /**
         * Sets additional content to include in the request.
         *
         * @param content additional content (e.g., process model XML)
         * @return this builder
         */
        public Builder content(String content) {
            this.content = content;
            return this;
        }

        /**
         * Sets the temperature parameter.
         *
         * @param temperature value between 0.0 and 2.0
         * @return this builder
         */
        public Builder temperature(double temperature) {
            this.temperature = temperature;
            return this;
        }

        /**
         * Sets the maximum number of tokens in the response.
         *
         * @param maxTokens maximum tokens (typically 1-4096)
         * @return this builder
         */
        public Builder maxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        /**
         * Builds the immutable LlmRequest.
         *
         * @return a new LlmRequest instance
         * @throws NullPointerException if required fields are missing
         */
        public LlmRequest build() {
            return new LlmRequest(this);
        }
    }

    /**
     * Returns a string representation for logging (truncates userPrompt for brevity).
     *
     * @return formatted string with key fields
     */
    @Override
    public String toString() {
        return "LlmRequest{" +
                "model='" + model + '\'' +
                ", userPrompt='" + (userPrompt != null ? userPrompt.substring(0, Math.min(50, userPrompt.length())) + "..." : "null") + '\'' +
                ", temperature=" + temperature +
                ", maxTokens=" + maxTokens +
                '}';
    }
}
