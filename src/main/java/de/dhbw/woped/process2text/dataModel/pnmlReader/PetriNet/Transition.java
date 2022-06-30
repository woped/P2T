/* (C)2022 */
package de.dhbw.woped.process2text.dataModel.pnmlReader.PetriNet;

public class Transition extends Element {
  public Transition(
      String id, String label, String role, String group, String type, String trigger) {
    super(id, label, role, group, type, trigger);
  }
}
