package de.dhbw.woped.process2text.bpmnProcessing.dataModel.camundaStructure;
import com.fasterxml.jackson.annotation.JsonProperty; 
import java.util.List; 
public class BpmnCollaboration{
    @JsonProperty("bpmn:messageFlow") 
    public List<BpmnMessageFlow> bpmnMessageFlow;
    @JsonProperty("bpmn:participant") 
    public List<BpmnParticipant> bpmnParticipant;
    public String id;
}
