package de.dhbw.woped.process2text.model.process;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OpenAiApiDTO {

  String apiKey;
  EnumGptModel gptModel;
  String prompt;
}
