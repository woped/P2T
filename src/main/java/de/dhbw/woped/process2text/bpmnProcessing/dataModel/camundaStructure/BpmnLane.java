package de.dhbw.woped.process2text.bpmnProcessing.dataModel.camundaStructure;
import com.fasterxml.jackson.annotation.JsonProperty; 
public class BpmnLane{
    public String name;
    public String id;
    @JsonProperty("bpmn:flowNodeRef") 
    public Object bpmnFlowNodeRef;
}
