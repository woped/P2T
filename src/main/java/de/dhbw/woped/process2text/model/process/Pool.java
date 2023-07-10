package de.dhbw.woped.process2text.model.process;

public class Pool {
  private String name;
  private String bpmnid;

  public Pool(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setBPMNId(String id) {
    this.bpmnid = id;
  }

  public String getBPMNId() {
    return bpmnid;
  }
}
