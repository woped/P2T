package de.dhbw.woped.process2text.model.reader.pnml.petri_net;

public class Transition extends Element {
  public Transition(
      String id, String label, String role, String group, String type, String trigger) {
    super(id, label, role, group, type, trigger);
  }
}
