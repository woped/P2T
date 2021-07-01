package de.dhbw.woped.process2text.bpmnProcessing.dataModel.camundaStructure;
import com.fasterxml.jackson.annotation.JsonProperty; 
public class BpmndiBPMNDiagram{
    public String id;
    @JsonProperty("bpmndi:BPMNPlane") 
    public BpmndiBPMNPlane bpmndiBPMNPlane;
}
