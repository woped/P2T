package de.dhbw.woped.process2text.model.process;

public enum EnumGptModel {
  GPT_3("gpt-3"),
  GPT_4("gpt-4"),
  GPT_4_TURBO("gpt-4-turbo");

  private final String gptModel;

  EnumGptModel(String gptModel) {
    this.gptModel = gptModel;
  }

  public String getModel() {
    return gptModel;
  }

  public static EnumGptModel getEnumGptModel(String gptModel) {
    switch (gptModel) {
      case "gpt-3":
        return EnumGptModel.GPT_3;
      case "gpt-4":
        return EnumGptModel.GPT_4;
      case "gpt-4-turbo":
        return EnumGptModel.GPT_4_TURBO;
      default:
        return EnumGptModel.GPT_4_TURBO;
    }
  }
}
