package de.dhbw.woped.process2text.bpmnProcessing.dataModel.camundaStructure;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class BpmnTask {
    @JsonProperty("bpmn:outgoing")
    public String bpmnOutgoing;
    public String id;
    public String name;
    @JsonProperty("bpmn:incoming")
    public Object bpmnIncoming;
}
