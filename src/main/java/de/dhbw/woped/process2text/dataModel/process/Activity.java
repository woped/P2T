package de.dhbw.woped.process2text.dataModel.process;

import java.util.ArrayList;

public class Activity extends Element {
    private int type;
    private ArrayList<Annotation> annotations;
    private ArrayList<Integer> attachedEvents;
    private String bpmnId;

    public Activity(int id, String label, Lane lane, Pool pool, int type) {
        super(id, label, lane, pool);
        this.type = type;
        annotations = new ArrayList<>();
        attachedEvents = new ArrayList<>();
    }
    public void addBPMNId(String id){
        this.bpmnId = id;
    }

    public String getBpmnId() {
        return bpmnId;
    }

    public boolean hasAttachedEvents() {
        return attachedEvents.size() > 0;
    }

    public ArrayList<Integer> getAttachedEvents() {
        return attachedEvents;
    }

    public ArrayList<Annotation> getAnnotations() {
        return annotations;
    }

    public int getType() {
        return type;
    }

    void addAnnotation(Annotation a) {
        annotations.add(a);
    }
}