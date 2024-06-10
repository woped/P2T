package de.dhbw.woped.process2text.model.process;

/**
 * Enum representing various GPT models available for use. Each enum constant corresponds to a
 * specific model name string.
 */
public enum EnumGptModel {
  GPT_4O("gpt-4o"),
  GPT_4O_2024_05_13("gpt-4o-2024-05-13"),
  GPT_4_TURBO("gpt-4-turbo"),
  GPT_4_TURBO_2024_04_09("gpt-4-turbo-2024-04-09"),
  GPT_4_TURBO_PREVIEW("gpt-4-turbo-preview"),
  GPT_4("gpt-4"),
  GPT_4_0125_PREVIEW("gpt-4-0125-preview"),
  GPT_4_1106_PREVIEW("gpt-4-1106-preview"),
  GPT_4_0613("gpt-4-0613"),
  GPT_3_5_TURBO_0125("gpt-3.5-turbo-0125"),
  GPT_3_5_TURBO("gpt-3.5-turbo"),
  GPT_3_5_TURBO_1106("gpt-3.5-turbo-1106"),
  GPT_3_5_TURBO_INSTRUCT("gpt-3.5-turbo-instruct");

  private final String gptModel;

  // Constructor to initialize the enum constant with the model name string
  EnumGptModel(String gptModel) {
    this.gptModel = gptModel;
  }

  // Getter method to retrieve the model name string
  public String getModel() {
    return gptModel;
  }

  /**
   * Static method to get the EnumGptModel corresponding to a given model name string.
   *
   * @param gptModel The model name string.
   * @return The corresponding EnumGptModel.
   * @throws IllegalArgumentException if the model name is not found.
   */
  public static EnumGptModel getEnumGptModel(String gptModel) {
    switch (gptModel) {
      case "gpt-4o":
        return EnumGptModel.GPT_4O;
      case "gpt-4o-2024-05-13":
        return EnumGptModel.GPT_4O_2024_05_13;
      case "gpt-4-turbo":
        return EnumGptModel.GPT_4_TURBO;
      case "gpt-4-turbo-2024-04-09":
        return EnumGptModel.GPT_4_TURBO_2024_04_09;
      case "gpt-4-turbo-preview":
        return EnumGptModel.GPT_4_TURBO_PREVIEW;
      case "gpt-4-0125-preview":
        return EnumGptModel.GPT_4_0125_PREVIEW;
      case "gpt-4-1106-preview":
        return EnumGptModel.GPT_4_1106_PREVIEW;
      case "gpt-4":
        return EnumGptModel.GPT_4;
      case "gpt-4-0613":
        return EnumGptModel.GPT_4_0613;
      case "gpt-3.5-turbo-0125":
        return EnumGptModel.GPT_3_5_TURBO_0125;
      case "gpt-3.5-turbo":
        return EnumGptModel.GPT_3_5_TURBO;
      case "gpt-3.5-turbo-1106":
        return EnumGptModel.GPT_3_5_TURBO_1106;
      case "gpt-3.5-turbo-instruct":
        return EnumGptModel.GPT_3_5_TURBO_INSTRUCT;
      default:
        throw new IllegalArgumentException("Model not found: " + gptModel);
    }
  }
}
