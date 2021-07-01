package de.dhbw.woped.process2text.bpmnProcessing.dataModel.camundaStructure;
import com.fasterxml.jackson.annotation.JsonProperty; 
import java.util.List; 
public class BpmnParallelGateway{
    @JsonProperty("bpmn:incoming") 
    public String bpmnIncoming;
    @JsonProperty("bpmn:outgoing") 
    public List<String> bpmnOutgoing;
    public String id;
}
