package de.dhbw.woped.process2text.model.process;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/** Data Transfer Object to hold OpenAI API related information. */
@Data
@Setter
@Getter
@AllArgsConstructor
public class OpenAiApiDTO {

  private String apiKey;
  private String gptModel;
  private String prompt;
}
