package de.dhbw.woped.process2text.bpmnProcessing.dataModel.camundaStructure;
import com.fasterxml.jackson.annotation.JsonProperty; 
public class BpmnIntermediateCatchEvent{
    @JsonProperty("bpmn:messageEventDefinition") 
    public BpmnMessageEventDefinition bpmnMessageEventDefinition;
    @JsonProperty("bpmn:incoming") 
    public String bpmnIncoming;
    @JsonProperty("bpmn:outgoing") 
    public String bpmnOutgoing;
    public String id;
}
