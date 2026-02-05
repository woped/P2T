package de.dhbw.woped.process2text.model.process;

/**
 * Data Transfer Object for LLM API request parameters.
 *
 * <p>This DTO encapsulates the parameters needed for making LLM API requests.
 * Despite its name, it supports multiple LLM providers including OpenAI,
 * Google Gemini, and LM Studio.
 *
 * <h2>Fields</h2>
 * <ul>
 *   <li>{@code apiKey} - API key for authentication (optional for LM Studio)</li>
 *   <li>{@code gptModel} - The specific model identifier to use</li>
 *   <li>{@code prompt} - The instruction prompt for text generation</li>
 *   <li>{@code provider} - The LLM provider name ("openai", "gemini", "lmstudio")</li>
 *   <li>{@code useRag} - Whether to enable RAG for prompt enrichment</li>
 * </ul>
 *
 * @author WoPeD Team
 * @deprecated This DTO is deprecated in favor of {@link de.dhbw.woped.process2text.service.llm.dto.LlmRequest}.
 *             Use the new LlmRequest builder pattern for creating LLM requests.
 * @see de.dhbw.woped.process2text.service.llm.dto.LlmRequest
 */
@Deprecated(forRemoval = true)
public class OpenAiApiDTO {

  /**
   * Constructs a new OpenAiApiDTO with all required parameters.
   *
   * @param apiKey   the API key for authentication
   * @param gptModel the model identifier (e.g., "gpt-4", "gemini-pro")
   * @param prompt   the instruction prompt for text generation
   * @param provider the LLM provider name
   * @param useRag   true to enable RAG prompt enrichment
   */
  public OpenAiApiDTO(
      String apiKey, String gptModel, String prompt, String provider, boolean useRag) {
    this.apiKey = apiKey;
    this.gptModel = gptModel;
    this.prompt = prompt;
    this.provider = provider;
    this.useRag = useRag;
  }

  private String apiKey;
  private String gptModel;
  private String prompt;
  private String provider;
  private boolean useRag;

  /**
   * Returns the API key for authentication.
   *
   * @return the API key
   */
  public String getApiKey() {
    return apiKey;
  }

  /**
   * Returns the model identifier.
   *
   * @return the model name
   */
  public String getGptModel() {
    return gptModel;
  }

  /**
   * Returns the instruction prompt.
   *
   * @return the prompt text
   */
  public String getPrompt() {
    return prompt;
  }

  /**
   * Returns the LLM provider name.
   *
   * @return the provider name
   */
  public String getProvider() {
    return provider;
  }

  /**
   * Returns whether RAG is enabled.
   *
   * @return true if RAG is enabled
   */
  public boolean isUseRag() {
    return useRag;
  }

  /**
   * Sets the model identifier.
   *
   * @param gptModel the model name
   */
  public void setGptModel(String gptModel) {
    this.gptModel = gptModel;
  }

  /**
   * Sets the API key.
   *
   * @param apiKey the API key
   */
  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  /**
   * Sets the instruction prompt.
   *
   * @param prompt the prompt text
   */
  public void setPrompt(String prompt) {
    this.prompt = prompt;
  }

  /**
   * Sets the LLM provider name.
   *
   * @param provider the provider name
   */
  public void setProvider(String provider) {
    this.provider = provider;
  }

  /**
   * Sets whether to enable RAG.
   *
   * @param useRag true to enable RAG
   */
  public void setUseRag(boolean useRag) {
    this.useRag = useRag;
  }
}
