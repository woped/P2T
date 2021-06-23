package de.dhbw.woped.process2text.bpmnProcessing.dataModel.camundaStructure;
import com.fasterxml.jackson.annotation.JsonProperty; 
import java.util.List; 
public class BpmnProcess{
    @JsonProperty("bpmn:sequenceFlow") 
    public List<BpmnSequenceFlow> bpmnSequenceFlow;
    @JsonProperty("bpmn:exclusiveGateway") 
    public List<BpmnExclusiveGateway> bpmnExclusiveGateway;
    @JsonProperty("bpmn:endEvent") 
    public BpmnEndEvent bpmnEndEvent;
    @JsonProperty("bpmn:startEvent") 
    public BpmnStartEvent bpmnStartEvent;
    @JsonProperty("bpmn:task") 
    public List<BpmnTask> bpmnTask;
    public String id;
    public boolean isExecutable;
    @JsonProperty("bpmn:parallelGateway") 
    public List<BpmnParallelGateway> bpmnParallelGateway;
    @JsonProperty("bpmn:laneSet") 
    public BpmnLaneSet bpmnLaneSet;
    @JsonProperty("bpmn:intermediateCatchEvent") 
    public BpmnIntermediateCatchEvent bpmnIntermediateCatchEvent;
}
