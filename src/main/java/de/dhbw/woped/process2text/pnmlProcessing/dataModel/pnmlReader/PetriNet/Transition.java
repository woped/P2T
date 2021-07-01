package de.dhbw.woped.process2text.pnmlProcessing.dataModel.pnmlReader.PetriNet;

public class Transition extends Element {
    public Transition(String id, String label, String role, String group, String type, String trigger) {
        super(id, label, role, group, type, trigger);
    }
}