package de.dhbw.woped.process2text.model.process;

public class Gateway extends Element {
  private final int type;
  private String bpmnId;

  public Gateway(int id, String label, Lane lane, Pool pool, int type) {
    super(id, label, lane, pool);
    this.type = type;
  }

  public void addBPMNId(String id) {
    this.bpmnId = id;
  }

  public String getBpmnId() {
    return bpmnId;
  }

  public int getType() {
    return type;
  }
}
