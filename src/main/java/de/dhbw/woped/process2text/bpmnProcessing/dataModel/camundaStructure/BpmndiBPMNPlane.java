package de.dhbw.woped.process2text.bpmnProcessing.dataModel.camundaStructure;
import com.fasterxml.jackson.annotation.JsonProperty; 
import java.util.List; 
public class BpmndiBPMNPlane{
    public String bpmnElement;
    @JsonProperty("bpmndi:BPMNShape") 
    public List<BpmndiBPMNShape> bpmndiBPMNShape;
    public String id;
    @JsonProperty("bpmndi:BPMNEdge") 
    public List<BpmndiBPMNEdge> bpmndiBPMNEdge;
}
