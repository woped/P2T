package de.dhbw.woped.process2text.bpmnProcessing.dataModel.camundaStructure;
import com.fasterxml.jackson.annotation.JsonProperty; 
public class BpmnEndEvent{
    @JsonProperty("bpmn:incoming") 
    public String bpmnIncoming;
    public String id;
}
