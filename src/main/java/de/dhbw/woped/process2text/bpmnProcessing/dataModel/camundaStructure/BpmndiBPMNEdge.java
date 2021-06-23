package de.dhbw.woped.process2text.bpmnProcessing.dataModel.camundaStructure;
import com.fasterxml.jackson.annotation.JsonProperty; 
import java.util.List; 
public class BpmndiBPMNEdge{
    @JsonProperty("di:waypoint") 
    public List<DiWaypoint> diWaypoint;
    public String bpmnElement;
    public String id;
    @JsonProperty("bpmndi:BPMNLabel") 
    public BpmndiBPMNLabel bpmndiBPMNLabel;
}
