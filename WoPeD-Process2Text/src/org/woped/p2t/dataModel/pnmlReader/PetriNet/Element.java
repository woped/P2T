package org.woped.p2t.dataModel.pnmlReader.PetriNet;

public abstract class Element {

    private String label;
    private String id;
    private String role;
    private String group;
    private String type;


    public Element(String id, String label, String role, String group, String type) {
        this.id = id;
        this.label = label;
        this.role = role;
        this.group = group;
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public String getGroup() { return group; }

    public void setGroup(String group) { this.group = group; }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}