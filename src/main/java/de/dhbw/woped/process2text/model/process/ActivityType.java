package de.dhbw.woped.process2text.model.process;

import java.util.Map;

public class ActivityType {

  public static final int NONE = 0;

  public static final Map<String, Integer> TYPE_MAP =
      Map.of("None", 0, "Manual", 1, "User", 0, "Subprocess", 2);
}
