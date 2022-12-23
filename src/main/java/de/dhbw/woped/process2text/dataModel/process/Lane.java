package de.dhbw.woped.process2text.dataModel.process;

public class Lane {
  private final String name;
  private final String exceptionString = "All";

  public Lane(String name, String pool) {
    if (pool.equals(exceptionString)) {
      this.name = name;
    } else {
      this.name = name + " from " + pool;
    }
  }

  public String getName() {
    return name;
  }
}
