package de.dhbw.woped.process2text.bpmnProcessing.dataModel.camundaStructure;
import com.fasterxml.jackson.annotation.JsonProperty; 
import java.util.List; 
public class BpmnLaneSet{
    @JsonProperty("bpmn:lane") 
    public List<BpmnLane> bpmnLane;
    public String id;
}
