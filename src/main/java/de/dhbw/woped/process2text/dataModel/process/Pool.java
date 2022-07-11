package de.dhbw.woped.process2text.dataModel.process;

public class Pool {
    private final String name;
    private String bpmnid;

    public Pool(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setBPMNId (String id){
        this.bpmnid = id;
    }

    public String getBPMNId () {
        return bpmnid;
    }
}