package de.dhbw.woped.process2text.bpmnProcessing.dataModel.camundaStructure;
import com.fasterxml.jackson.annotation.JsonProperty; 
public class BpmnStartEvent{
    @JsonProperty("bpmn:outgoing") 
    public String bpmnOutgoing;
    public String id;
    @JsonProperty("bpmn:messageEventDefinition") 
    public BpmnMessageEventDefinition bpmnMessageEventDefinition;
}
